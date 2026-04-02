package com.tools.il2fusion.config

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Mediates data access between the UI and HookConfigStore to keep logic centralized.
 */
class HookConfigRepository(
    context: Context
) {
    private val appContext = context.applicationContext

    /**
     * Loads stored target method list and dump mode flag from the shared content provider.
     */
    suspend fun loadConfig(): HookConfigPayload = withContext(Dispatchers.IO) {
        val savedTargets = HookConfigStore.loadTargetsForApp(appContext)
        val dumpMode = HookConfigStore.loadDumpModeForApp(appContext)
        val hookFramework = HookConfigStore.loadHookFrameworkForApp(appContext)
        val targetsJson = HookConfigStore.loadTargetsJsonForApp(appContext)
        HookConfigPayload(
            targets = savedTargets,
            dumpModeEnabled = dumpMode,
            hookFramework = hookFramework,
            targetsJson = targetsJson
        )
    }

    /**
     * Persists the dump mode flag through the content provider.
     */
    suspend fun saveDumpMode(enabled: Boolean) = withContext(Dispatchers.IO) {
        HookConfigStore.saveDumpMode(appContext, enabled)
        HookConfigChangeBus.notifyChanged()
    }

    suspend fun saveHookFramework(framework: HookFramework) = withContext(Dispatchers.IO) {
        HookConfigStore.saveHookFramework(appContext, framework)
        HookConfigChangeBus.notifyChanged()
    }

    /**
     * Persists the target method list through the content provider.
     */
    suspend fun saveTargets(targets: List<String>) = withContext(Dispatchers.IO) {
        HookConfigStore.saveTargets(appContext, targets)
        HookConfigChangeBus.notifyChanged()
    }

    suspend fun saveTargetsJson(json: String) = withContext(Dispatchers.IO) {
        HookConfigStore.saveTargetsJson(appContext, json)
        HookConfigChangeBus.notifyChanged()
    }
}

/**
 * Represents stored configuration values used by both the UI and native hook.
 */
data class HookConfigPayload(
    val targets: List<String>,
    val dumpModeEnabled: Boolean,
    val hookFramework: HookFramework,
    val targetsJson: String
)
