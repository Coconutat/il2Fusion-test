package com.tools.il2fusion.core.i18n

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.languagePreferencesDataStore by preferencesDataStore(name = "language_preferences")

class LanguagePreferencesRepository(
    context: Context
) {
    private val appContext = context.applicationContext

    val settingsFlow: Flow<LanguageSettings> = appContext.languagePreferencesDataStore.data.map { preferences ->
        LanguageSettings(
            mode = LanguageMode.fromStorageValue(preferences[LANGUAGE_MODE_KEY]),
            manualLanguage = AppLanguage.fromLanguageTag(preferences[MANUAL_LANGUAGE_KEY])
        )
    }

    suspend fun loadSettings(): LanguageSettings = settingsFlow.first()

    suspend fun updateMode(mode: LanguageMode) {
        appContext.languagePreferencesDataStore.edit { preferences ->
            preferences[LANGUAGE_MODE_KEY] = mode.storageValue
        }
    }

    suspend fun updateManualLanguage(language: AppLanguage) {
        appContext.languagePreferencesDataStore.edit { preferences ->
            preferences[MANUAL_LANGUAGE_KEY] = language.languageTag
        }
    }

    private companion object {
        val LANGUAGE_MODE_KEY = stringPreferencesKey("language_mode")
        val MANUAL_LANGUAGE_KEY = stringPreferencesKey("manual_language")
    }
}
