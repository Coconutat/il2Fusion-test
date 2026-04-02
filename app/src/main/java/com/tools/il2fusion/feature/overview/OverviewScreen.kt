package com.tools.il2fusion.feature.overview

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.tools.il2fusion.R
import com.tools.il2fusion.core.design.FeatureScreenSurface
import com.tools.il2fusion.core.design.SectionCard
import com.tools.il2fusion.feature.common.HookSummaryCard
import com.tools.il2fusion.feature.common.UsageNoteCard

@Composable
fun OverviewRoute(
    modifier: Modifier = Modifier,
    viewModel: OverviewViewModel = viewModel()
) {
    val state = viewModel.uiState.collectAsStateWithLifecycle().value

    FeatureScreenSurface(modifier = modifier) {
        if (state.isLoading) {
            CircularProgressIndicator()
        }
        HookSummaryCard(summary = state.summary)
        SectionCard(
            title = stringResource(R.string.overview_flow_title),
            subtitle = stringResource(R.string.overview_flow_subtitle)
        ) {
            Text(
                text = stringResource(R.string.overview_flow_body),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.fillMaxWidth()
            )
        }
        UsageNoteCard()
    }
}
