package com.tools.il2fusion.feature.mode

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.tools.il2fusion.R
import com.tools.il2fusion.config.HookFramework
import com.tools.il2fusion.core.design.FeatureScreenSurface
import com.tools.il2fusion.core.design.SectionCard
import com.tools.il2fusion.feature.common.HookSummaryCard

@Composable
fun ModeRoute(
    modifier: Modifier = Modifier,
    viewModel: ModeViewModel = viewModel()
) {
    val state = viewModel.uiState.collectAsStateWithLifecycle().value
    val context = LocalContext.current

    LaunchedEffect(viewModel) {
        viewModel.events.collect { message ->
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        }
    }

    FeatureScreenSurface(modifier = modifier) {
        if (state.isLoading) {
            CircularProgressIndicator()
        }
        HookSummaryCard(summary = state.summary)
        ModeToggleCard(
            title = stringResource(R.string.mode_dump_title),
            description = if (state.dumpModeEnabled) {
                stringResource(R.string.mode_dump_enabled_desc)
            } else {
                stringResource(R.string.mode_dump_disabled_desc)
            },
            checked = state.dumpModeEnabled,
            onCheckedChange = viewModel::onDumpModeChanged
        )
        ModeToggleCard(
            title = stringResource(R.string.mode_framework_title),
            description = if (state.hookFramework == HookFramework.Dobby) {
                stringResource(R.string.mode_framework_dobby_desc)
            } else {
                stringResource(R.string.mode_framework_and64_desc)
            },
            checked = state.hookFramework == HookFramework.Dobby,
            onCheckedChange = viewModel::onHookFrameworkChanged
        )
    }
}

@Composable
private fun ModeToggleCard(
    title: String,
    description: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    SectionCard(title = title, subtitle = description) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if (checked) stringResource(R.string.common_enabled) else stringResource(R.string.common_disabled),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Switch(checked = checked, onCheckedChange = onCheckedChange)
        }
    }
}
