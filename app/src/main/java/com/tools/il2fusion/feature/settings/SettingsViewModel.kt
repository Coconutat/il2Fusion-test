package com.tools.il2fusion.feature.settings

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.tools.il2fusion.BuildConfig
import com.tools.il2fusion.R
import com.tools.il2fusion.core.i18n.AppLanguage
import com.tools.il2fusion.core.i18n.AppLocaleManager
import com.tools.il2fusion.core.i18n.LanguageMode
import com.tools.il2fusion.core.i18n.LanguagePreferencesRepository
import com.tools.il2fusion.core.update.ApkDownloadManager
import com.tools.il2fusion.core.update.ApkDownloadResult
import com.tools.il2fusion.core.update.ApkInstallResult
import com.tools.il2fusion.core.update.ApkInstaller
import com.tools.il2fusion.core.update.UpdateRepository
import com.tools.il2fusion.core.update.model.CheckUpdateRequest
import com.tools.il2fusion.core.update.model.UpdateCheckResult
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

class SettingsViewModel(application: Application) : AndroidViewModel(application) {
    private val languageRepository = LanguagePreferencesRepository(application)
    private val updateRepository = UpdateRepository()
    private val downloadManager = ApkDownloadManager(application)
    private val apkInstaller = ApkInstaller(application)

    private val _uiState = MutableStateFlow(
        SettingsUiState(
            versionName = BuildConfig.VERSION_NAME,
            versionCode = BuildConfig.VERSION_CODE
        )
    )
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    private val _events = Channel<String>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    init {
        observeLanguageSettings()
    }

    private fun observeLanguageSettings() {
        viewModelScope.launch {
            languageRepository.settingsFlow.collect { settings ->
                _uiState.value = _uiState.value.copy(
                    languageMode = settings.mode,
                    manualLanguage = settings.manualLanguage,
                    effectiveLanguage = AppLocaleManager.resolveEffectiveLanguage(
                        getApplication(),
                        settings
                    )
                )
            }
        }
    }

    fun onLanguageModeSelected(mode: LanguageMode) {
        viewModelScope.launch {
            if (mode == LanguageMode.Manual) {
                val currentSettings = languageRepository.loadSettings()
                val effectiveLanguage = AppLocaleManager.resolveEffectiveLanguage(
                    getApplication(),
                    currentSettings
                )
                languageRepository.updateManualLanguage(effectiveLanguage)
            }
            languageRepository.updateMode(mode)
            AppLocaleManager.applySettings(getApplication(), languageRepository.loadSettings())
        }
    }

    fun onManualLanguageSelected(language: AppLanguage) {
        viewModelScope.launch {
            languageRepository.updateManualLanguage(language)
            languageRepository.updateMode(LanguageMode.Manual)
            AppLocaleManager.applySettings(getApplication(), languageRepository.loadSettings())
            _events.send(getApplication<Application>().getString(R.string.settings_language_switched))
        }
    }

    fun checkForUpdates() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isCheckingUpdate = true,
                updateStatusText = null
            )
            when (val result = updateRepository.checkForUpdate(buildUpdateRequest())) {
                is UpdateCheckResult.Available -> {
                    _uiState.value = _uiState.value.copy(
                        isCheckingUpdate = false,
                        availableUpdate = result.info,
                        updateStatusText = getApplication<Application>().getString(
                            R.string.settings_update_available_inline,
                            result.info.latestVersionName
                        )
                    )
                }

                is UpdateCheckResult.UpToDate -> {
                    _uiState.value = _uiState.value.copy(
                        isCheckingUpdate = false,
                        availableUpdate = null,
                        updateStatusText = getApplication<Application>().getString(R.string.settings_update_latest)
                    )
                }

                is UpdateCheckResult.Failure -> {
                    _uiState.value = _uiState.value.copy(
                        isCheckingUpdate = false,
                        updateStatusText = when {
                            result.isRateLimited -> {
                                getApplication<Application>().getString(R.string.settings_update_rate_limited)
                            }

                            result.message.isBlank() -> {
                                getApplication<Application>().getString(R.string.settings_update_failed_generic)
                            }

                            else -> result.message
                        }
                    )
                }
            }
        }
    }

    fun downloadAndInstallUpdate() {
        val updateInfo = _uiState.value.availableUpdate ?: return
        val asset = updateInfo.asset
        if (asset == null) {
            viewModelScope.launch {
                _events.send(getApplication<Application>().getString(R.string.settings_update_no_apk))
            }
            return
        }
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isDownloadingUpdate = true,
                downloadProgress = 0
            )
            when (val result = downloadManager.downloadApk(asset) { progress ->
                _uiState.value = _uiState.value.copy(downloadProgress = progress)
            }) {
                is ApkDownloadResult.Failure -> {
                    _uiState.value = _uiState.value.copy(
                        isDownloadingUpdate = false,
                        updateStatusText = result.message.ifBlank {
                            getApplication<Application>().getString(R.string.settings_update_download_failed)
                        }
                    )
                }

                is ApkDownloadResult.Success -> {
                    _uiState.value = _uiState.value.copy(
                        isDownloadingUpdate = false,
                        downloadProgress = 100,
                        updateStatusText = getApplication<Application>().getString(R.string.settings_update_download_complete)
                    )
                    when (val installResult = apkInstaller.installApk(result.file)) {
                        ApkInstallResult.LaunchedInstaller -> {
                            _events.send(getApplication<Application>().getString(R.string.settings_update_install_started))
                        }

                        ApkInstallResult.PermissionRequired -> {
                            _events.send(getApplication<Application>().getString(R.string.settings_update_permission_required))
                        }

                        is ApkInstallResult.Failure -> {
                            _events.send(
                                installResult.message.ifBlank {
                                    getApplication<Application>().getString(R.string.settings_update_install_failed)
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    private fun buildUpdateRequest(): CheckUpdateRequest {
        return CheckUpdateRequest(
            currentVersionName = BuildConfig.VERSION_NAME,
            currentVersionCode = BuildConfig.VERSION_CODE
        )
    }
}
