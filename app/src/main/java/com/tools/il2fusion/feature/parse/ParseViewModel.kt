package com.tools.il2fusion.feature.parse

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.tools.il2fusion.R
import com.tools.il2fusion.config.HookConfigChangeBus
import com.tools.il2fusion.config.HookConfigRepository
import com.tools.il2fusion.utils.DumpFileParser
import com.tools.il2fusion.utils.HookTargetUtils
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

class ParseViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = HookConfigRepository(application)
    private val dumpFileParser = DumpFileParser()

    private val _uiState = MutableStateFlow(ParseUiState())
    val uiState: StateFlow<ParseUiState> = _uiState.asStateFlow()

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

    fun onFilePicked(uri: Uri?) {
        if (uri == null) {
            viewModelScope.launch {
                _events.send(getApplication<Application>().getString(R.string.message_no_file_selected))
            }
            return
        }
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                val result = dumpFileParser.extractTargets(getApplication(), uri, Int.MAX_VALUE)
                val methods = HookTargetUtils.normalizeInputs(result.entries.map { it.functionName })
                if (methods.isEmpty()) {
                    _events.send(getApplication<Application>().getString(R.string.message_parse_empty))
                    return@launch
                }
                repository.saveTargets(methods)
                if (result.jsonText.isNotBlank()) {
                    repository.saveTargetsJson(result.jsonText)
                }
                _events.send(
                    getApplication<Application>().getString(R.string.message_parse_success, methods.size)
                )
                result.savedJsonPath?.let { path ->
                    _events.send(
                        getApplication<Application>().getString(R.string.message_json_saved, path)
                    )
                }
                refresh()
            } finally {
                _uiState.value = _uiState.value.copy(isLoading = false)
            }
        }
    }

    fun onSave() {
        viewModelScope.launch {
            val cleaned = HookTargetUtils.normalizeInputs(_uiState.value.methods)
            if (cleaned.isEmpty()) {
                _events.send(getApplication<Application>().getString(R.string.message_parse_save_empty))
                return@launch
            }
            repository.saveTargets(cleaned)
            _events.send(
                getApplication<Application>().getString(R.string.message_save_success, cleaned.size)
            )
            refresh()
        }
    }

    private fun refresh() {
        viewModelScope.launch {
            val payload = repository.loadConfig()
            _uiState.value = ParseUiState(
                methods = HookTargetUtils.formatInputs(payload.targets),
                savedCount = payload.targets.size,
                hasTargetsJson = payload.targetsJson.isNotBlank()
            )
        }
    }
}
