package com.tools.il2fusion.config

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Mediates data access between the UI and HookConfigStore to keep logic centralized.
 */
class HookConfigRepository {

    /**
     * Loads stored target method list and dump mode flag from the shared content provider.
     */
    suspend fun loadConfig(context: Context): HookConfigPayload = withContext(Dispatchers.IO) {
        val savedTargets = HookConfigStore.loadTargetsForApp(context)
        val dumpMode = HookConfigStore.loadDumpModeForApp(context)
        val hookFramework = HookConfigStore.loadHookFrameworkForApp(context)
        val targetsJson = HookConfigStore.loadTargetsJsonForApp(context)
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
    suspend fun saveDumpMode(context: Context, enabled: Boolean) = withContext(Dispatchers.IO) {
        HookConfigStore.saveDumpMode(context, enabled)
    }

    suspend fun saveHookFramework(context: Context, framework: HookFramework) = withContext(Dispatchers.IO) {
        HookConfigStore.saveHookFramework(context, framework)
    }

    /**
     * Persists the target method list through the content provider.
     */
    suspend fun saveTargets(context: Context, targets: List<String>) = withContext(Dispatchers.IO) {
        HookConfigStore.saveTargets(context, targets)
    }

    suspend fun saveTargetsJson(context: Context, json: String) = withContext(Dispatchers.IO) {
        HookConfigStore.saveTargetsJson(context, json)
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
