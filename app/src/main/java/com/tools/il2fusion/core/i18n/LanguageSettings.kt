package com.tools.il2fusion.core.i18n

data class LanguageSettings(
    val mode: LanguageMode = LanguageMode.Auto,
    val manualLanguage: AppLanguage = AppLanguage.English
)
