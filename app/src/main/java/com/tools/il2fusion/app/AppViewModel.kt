package com.tools.il2fusion.app

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.tools.il2fusion.BuildConfig
import com.tools.il2fusion.core.update.UpdateRepository
import com.tools.il2fusion.core.update.model.CheckUpdateRequest
import com.tools.il2fusion.core.update.model.UpdateCheckResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AppViewModel(application: Application) : AndroidViewModel(application) {
    private val updateRepository = UpdateRepository()

    private val _uiState = MutableStateFlow(AppUiState())
    val uiState: StateFlow<AppUiState> = _uiState.asStateFlow()

    init {
        checkForUpdatesOnLaunch()
    }

    fun dismissStartupUpdate() {
        _uiState.value = _uiState.value.copy(startupUpdateInfo = null)
    }

    private fun checkForUpdatesOnLaunch() {
        viewModelScope.launch {
            when (
                val result = updateRepository.checkForUpdate(
                    CheckUpdateRequest(
                        currentVersionName = BuildConfig.VERSION_NAME,
                        currentVersionCode = BuildConfig.VERSION_CODE
                    )
                )
            ) {
                is UpdateCheckResult.Available -> {
                    _uiState.value = AppUiState(startupUpdateInfo = result.info)
                }

                is UpdateCheckResult.UpToDate,
                is UpdateCheckResult.Failure -> Unit
            }
        }
    }
}
