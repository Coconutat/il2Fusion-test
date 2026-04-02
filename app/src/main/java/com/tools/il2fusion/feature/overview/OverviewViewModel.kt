package com.tools.il2fusion.feature.overview

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.tools.il2fusion.config.HookConfigChangeBus
import com.tools.il2fusion.config.HookConfigRepository
import com.tools.il2fusion.feature.common.toSummaryUiModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class OverviewViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = HookConfigRepository(application)

    private val _uiState = MutableStateFlow(OverviewUiState())
    val uiState: StateFlow<OverviewUiState> = _uiState.asStateFlow()

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

    fun refresh() {
        viewModelScope.launch {
            val payload = repository.loadConfig()
            _uiState.value = OverviewUiState(
                isLoading = false,
                summary = payload.toSummaryUiModel()
            )
        }
    }
}
