package com.tools.il2fusion.core.i18n

import android.content.Context
import android.content.res.Resources
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat

object AppLocaleManager {
    suspend fun applyPersistedSettings(context: Context) {
        val settings = LanguagePreferencesRepository(context).loadSettings()
        applySettings(context, settings)
    }

    fun applySettings(
        context: Context,
        settings: LanguageSettings
    ) {
        val locales = if (settings.mode == LanguageMode.Auto) {
            LocaleListCompat.forLanguageTags(detectSystemLanguage().languageTag)
        } else {
            LocaleListCompat.forLanguageTags(settings.manualLanguage.languageTag)
        }
        AppCompatDelegate.setApplicationLocales(locales)
    }

    fun resolveEffectiveLanguage(
        context: Context,
        settings: LanguageSettings
    ): AppLanguage {
        return if (settings.mode == LanguageMode.Auto) {
            detectSystemLanguage()
        } else {
            settings.manualLanguage
        }
    }

    fun detectSystemLanguage(): AppLanguage {
        val systemLocales = LocaleListCompat.wrap(Resources.getSystem().configuration.locales)
        val locale = if (systemLocales.isEmpty) {
            java.util.Locale.getDefault()
        } else {
            systemLocales[0] ?: java.util.Locale.getDefault()
        }
        return AppLanguage.fromLocale(locale)
    }
}
