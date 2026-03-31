package com.tools.il2fusion.ui

import android.content.Context
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.tools.il2fusion.ui.theme.Il2FusionTheme
import kotlinx.coroutines.flow.collect

@Composable
fun HookConfigApp(viewModel: HookConfigViewModel = viewModel()) {
    Il2FusionTheme {
        val context = LocalContext.current
        val focusManager = LocalFocusManager.current
        val state by viewModel.state.collectAsState()
        val snackbarHostState = remember { SnackbarHostState() }
        val filePickerLauncher = rememberLauncherForActivityResult(
            ActivityResultContracts.OpenDocument()
        ) { uri ->
            viewModel.onFilePicked(context, uri)
        }
        val tabs = remember { SideTab.entries.toList() }
        var selectedTab by rememberSaveable { mutableStateOf(SideTab.Overview) }

        LaunchedEffect(Unit) {
            viewModel.loadInitial(context)
        }

        LaunchedEffect(Unit) {
            viewModel.events.collect { event ->
                when (event) {
                    is HookConfigEvent.ShowMessage -> snackbarHostState.showSnackbar(event.text)
                }
            }
        }

        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                AppChrome(
                    state = state,
                    selectedTab = selectedTab,
                    tabs = tabs,
                    onSelect = { selectedTab = it }
                )
            },
            snackbarHost = { SnackbarHost(snackbarHostState) }
        ) { inner ->
            ContentHost(
                selectedTab = selectedTab,
                state = state,
                context = context,
                filePickerLauncher = filePickerLauncher,
                viewModel = viewModel,
                focusManagerClear = { focusManager.clearFocus() },
                modifier = Modifier
                    .fillMaxSize()
                    .padding(inner)
            )
        }
    }
}

@Composable
private fun AppChrome(
    state: HookConfigState,
    selectedTab: SideTab,
    tabs: List<SideTab>,
    onSelect: (SideTab) -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(colorScheme.surface.copy(alpha = 0.96f))
            .statusBarsPadding()
            .padding(horizontal = 18.dp, vertical = 12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Top
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = "il2Fusion",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold),
                    color = colorScheme.onSurface
                )
                Text(
                    text = "Unity 文本拦截工具",
                    style = MaterialTheme.typography.bodySmall,
                    color = colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
            StatusPill(
                text = if (state.dumpModeEnabled) "Dump 模式" else state.hookFramework.displayName
            )
        }

        TabStrip(
            tabs = tabs,
            selected = selectedTab,
            onSelect = onSelect,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp)
        )
    }
}

@Composable
private fun StatusPill(
    text: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
            contentColor = MaterialTheme.colorScheme.primary
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
        shape = androidx.compose.foundation.shape.RoundedCornerShape(999.dp)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
        )
    }
}

@Composable
private fun TabStrip(
    tabs: List<SideTab>,
    selected: SideTab,
    onSelect: (SideTab) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.92f)
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        shape = androidx.compose.foundation.shape.RoundedCornerShape(22.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            tabs.forEach { tab ->
                val selectedTab = tab == selected
                val containerColor = if (selectedTab) {
                    MaterialTheme.colorScheme.primary.copy(alpha = 0.16f)
                } else {
                    Color.Transparent
                }
                val contentColor = if (selectedTab) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                }
                val borderColor = if (selectedTab) {
                    MaterialTheme.colorScheme.primary.copy(alpha = 0.28f)
                } else {
                    Color.Transparent
                }
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .heightIn(min = 50.dp)
                        .padding(horizontal = 3.dp)
                        .clickable { onSelect(tab) },
                    colors = CardDefaults.cardColors(
                        containerColor = containerColor,
                        contentColor = contentColor
                    ),
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, borderColor),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 13.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = tab.title,
                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                            color = contentColor
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ContentHost(
    selectedTab: SideTab,
    state: HookConfigState,
    context: Context,
    filePickerLauncher: ActivityResultLauncher<Array<String>>,
    viewModel: HookConfigViewModel,
    focusManagerClear: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .pointerInput(Unit) {
                detectTapGestures(onTap = { focusManagerClear() })
            }
    ) {
        when (selectedTab) {
            SideTab.Overview -> HookOverviewScreen(state = state)
            SideTab.Dump -> DumpModeScreen(
                state = state,
                onDumpModeChanged = { viewModel.onDumpModeChanged(context, it) },
                onHookFrameworkChanged = { viewModel.onHookFrameworkChanged(context, it) }
            )
            SideTab.Parse -> ParseTextScreen(
                state = state,
                onPickFile = { filePickerLauncher.launch(arrayOf("*/*")) },
                onSave = { viewModel.onSave(context) },
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}

enum class SideTab(val title: String) {
    Overview("总览"),
    Dump("模式"),
    Parse("解析")
}
