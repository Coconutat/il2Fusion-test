package com.tools.il2fusion.feature.mode

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.tools.il2fusion.R
import com.tools.il2fusion.config.HookConfigChangeBus
import com.tools.il2fusion.config.HookConfigRepository
import com.tools.il2fusion.config.HookFramework
import com.tools.il2fusion.feature.common.toSummaryUiModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

class ModeViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = HookConfigRepository(application)

    private val _uiState = MutableStateFlow(ModeUiState())
    val uiState: StateFlow<ModeUiState> = _uiState.asStateFlow()

    private val _events = Channel<String>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    init {
        refresh()
        observeConfigChanges()
    }

    private fun observeConfigChanges() {
        viewModelScope.launch {
            HookConfigChangeBus.changes.collect {
                refresh()
            }
        }
    }

    fun onDumpModeChanged(enabled: Boolean) {
        viewModelScope.launch {
            repository.saveDumpMode(enabled)
            _events.send(
                getApplication<Application>().getString(
                    if (enabled) R.string.message_dump_mode_enabled else R.string.message_dump_mode_disabled
                )
            )
        }
    }

    fun onHookFrameworkChanged(useDobby: Boolean) {
        viewModelScope.launch {
            val framework = if (useDobby) HookFramework.Dobby else HookFramework.And64InlineHook
            repository.saveHookFramework(framework)
            _events.send(
                getApplication<Application>().getString(
                    if (framework == HookFramework.Dobby) {
                        R.string.message_hook_framework_dobby
                    } else {
                        R.string.message_hook_framework_and64
                    }
                )
            )
        }
    }

    private fun refresh() {
        viewModelScope.launch {
            val payload = repository.loadConfig()
            _uiState.value = ModeUiState(
                isLoading = false,
                summary = payload.toSummaryUiModel(),
                dumpModeEnabled = payload.dumpModeEnabled,
                hookFramework = payload.hookFramework
            )
        }
    }
}
