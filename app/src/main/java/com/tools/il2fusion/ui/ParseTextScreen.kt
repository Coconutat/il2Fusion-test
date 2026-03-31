package com.tools.il2fusion.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun ParseTextScreen(
    state: HookConfigState,
    onPickFile: () -> Unit,
    onSave: () -> Unit,
    modifier: Modifier = Modifier
) {
    ScreenSurface(modifier = modifier) {
            // 解析 dump.cs 并展示方法列表
            FileCard(
                isLoading = state.isLoading,
                onPickFile = onPickFile
            )

            MethodListCard(
                methods = state.methodInputs,
                savedCount = state.savedCount
            )

            SaveRow(
                onSave = onSave
            )

            FooterNote()
    }
}
