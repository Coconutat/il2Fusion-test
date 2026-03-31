package com.tools.il2fusion.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun DumpModeScreen(
    state: HookConfigState,
    onDumpModeChanged: (Boolean) -> Unit,
    onHookFrameworkChanged: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    ScreenSurface(modifier = modifier) {
            HeaderCard(
                dumpModeEnabled = state.dumpModeEnabled,
                savedCount = state.savedCount,
                hookFramework = state.hookFramework,
                hasTargetsJson = state.hasTargetsJson
            )
            ModeCard(
                dumpModeEnabled = state.dumpModeEnabled,
                hookFramework = state.hookFramework,
                onDumpModeChanged = onDumpModeChanged,
                onHookFrameworkChanged = onHookFrameworkChanged
            )
    }
}
