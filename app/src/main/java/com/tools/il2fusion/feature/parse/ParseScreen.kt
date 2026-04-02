package com.tools.il2fusion.feature.parse

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.tools.il2fusion.R
import com.tools.il2fusion.core.design.FeatureScreenSurface
import com.tools.il2fusion.core.design.SectionCard
import com.tools.il2fusion.core.design.StatusBadge
import com.tools.il2fusion.feature.common.UsageNoteCard

@Composable
fun ParseRoute(
    modifier: Modifier = Modifier,
    viewModel: ParseViewModel = viewModel()
) {
    val state = viewModel.uiState.collectAsStateWithLifecycle().value
    val context = LocalContext.current
    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
        onResult = viewModel::onFilePicked
    )

    LaunchedEffect(viewModel) {
        viewModel.events.collect { message ->
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        }
    }

    FeatureScreenSurface(modifier = modifier) {
        SectionCard(
            title = stringResource(R.string.parse_import_title),
            subtitle = stringResource(R.string.parse_import_subtitle)
        ) {
            Button(
                onClick = { filePickerLauncher.launch(arrayOf("*/*")) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = stringResource(R.string.parse_pick_file))
            }
            AnimatedVisibility(visible = state.isLoading) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            }
        }
        SectionCard(
            title = stringResource(R.string.parse_results_title),
            subtitle = stringResource(R.string.parse_results_subtitle, state.savedCount)
        ) {
            StatusBadge(
                text = if (state.hasTargetsJson) {
                    stringResource(R.string.summary_json_cached)
                } else {
                    stringResource(R.string.summary_json_missing)
                }
            )
            if (state.methods.isEmpty()) {
                Text(
                    text = stringResource(R.string.parse_results_empty),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    state.methods.forEachIndexed { index, method ->
                        Text(
                            text = "${index + 1}. $method",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f),
                                    shape = RoundedCornerShape(14.dp)
                                )
                                .padding(horizontal = 12.dp, vertical = 10.dp)
                        )
                    }
                }
            }
            Button(
                onClick = viewModel::onSave,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = stringResource(R.string.parse_save))
            }
        }
        UsageNoteCard()
    }
}
