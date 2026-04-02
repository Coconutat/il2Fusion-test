package com.tools.il2fusion.feature.settings

import com.tools.il2fusion.core.i18n.AppLanguage
import com.tools.il2fusion.core.i18n.LanguageMode
import com.tools.il2fusion.core.update.model.UpdateInfo

data class SettingsUiState(
    val languageMode: LanguageMode = LanguageMode.Auto,
    val manualLanguage: AppLanguage = AppLanguage.English,
    val effectiveLanguage: AppLanguage = AppLanguage.English,
    val versionName: String = "",
    val versionCode: Int = 0,
    val isCheckingUpdate: Boolean = false,
    val isDownloadingUpdate: Boolean = false,
    val downloadProgress: Int = 0,
    val updateStatusText: String? = null,
    val availableUpdate: UpdateInfo? = null
)
