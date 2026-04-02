package com.tools.il2fusion.feature.common

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.tools.il2fusion.R
import com.tools.il2fusion.core.design.SectionCard
import com.tools.il2fusion.core.design.StatusBadge

@Composable
fun HookSummaryCard(
    summary: HookSummaryUiModel,
    modifier: Modifier = Modifier
) {
    SectionCard(
        title = stringResource(R.string.overview_runtime_title),
        subtitle = if (summary.dumpModeEnabled) {
            stringResource(R.string.overview_runtime_dump_subtitle)
        } else {
            stringResource(R.string.overview_runtime_hook_subtitle)
        },
        modifier = modifier
    ) {
        Text(
            text = if (summary.dumpModeEnabled) {
                stringResource(R.string.overview_runtime_dump_body)
            } else {
                stringResource(R.string.overview_runtime_hook_body)
            },
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            StatusBadge(text = stringResource(R.string.summary_saved_methods, summary.savedCount))
            StatusBadge(
                text = if (summary.dumpModeEnabled) {
                    stringResource(R.string.summary_mode_dump_only)
                } else {
                    stringResource(R.string.summary_mode_intercept)
                }
            )
            StatusBadge(text = stringResource(summary.hookFramework.displayNameRes))
            StatusBadge(
                text = if (summary.hasTargetsJson) {
                    stringResource(R.string.summary_json_cached)
                } else {
                    stringResource(R.string.summary_json_missing)
                }
            )
        }
        Text(
            text = stringResource(R.string.overview_runtime_footer),
            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
