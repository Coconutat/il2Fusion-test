package com.tools.il2fusion

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.activity.compose.setContent
import com.tools.il2fusion.app.Il2FusionApp
import com.tools.il2fusion.core.i18n.AppLocaleManager
import kotlinx.coroutines.runBlocking

/**
 * Hosts the Compose entry point for configuring hook parameters.
 */
class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        runBlocking {
            AppLocaleManager.applyPersistedSettings(this@MainActivity)
        }
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Il2FusionApp()
        }
    }
}
