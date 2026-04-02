package com.tools.il2fusion

import android.app.Application
import com.tools.il2fusion.core.i18n.AppLocaleManager
import kotlinx.coroutines.runBlocking

class Il2FusionApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        runBlocking {
            AppLocaleManager.applyPersistedSettings(this@Il2FusionApplication)
        }
    }
}
