package com.tools.il2fusion.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun HookOverviewScreen(
    state: HookConfigState,
    modifier: Modifier = Modifier
) {
    ScreenSurface(modifier = modifier) {
            HeaderCard(
                dumpModeEnabled = state.dumpModeEnabled,
                savedCount = state.savedCount,
                hookFramework = state.hookFramework,
                hasTargetsJson = state.hasTargetsJson
            )
            FooterNote()
    }
}
