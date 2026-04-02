package com.tools.il2fusion.feature.common

import com.tools.il2fusion.config.HookConfigPayload
import com.tools.il2fusion.config.HookFramework

data class HookSummaryUiModel(
    val dumpModeEnabled: Boolean = false,
    val savedCount: Int = 0,
    val hookFramework: HookFramework = HookFramework.And64InlineHook,
    val hasTargetsJson: Boolean = false
)

fun HookConfigPayload.toSummaryUiModel(): HookSummaryUiModel {
    return HookSummaryUiModel(
        dumpModeEnabled = dumpModeEnabled,
        savedCount = targets.size,
        hookFramework = hookFramework,
        hasTargetsJson = targetsJson.isNotBlank()
    )
}
