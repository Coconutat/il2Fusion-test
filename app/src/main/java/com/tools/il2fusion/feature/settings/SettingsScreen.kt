package com.tools.il2fusion.feature.settings

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.tools.il2fusion.R
import com.tools.il2fusion.core.common.TimeFormatter
import com.tools.il2fusion.core.design.FeatureScreenSurface
import com.tools.il2fusion.core.design.SectionCard
import com.tools.il2fusion.core.i18n.AppLanguage
import com.tools.il2fusion.core.i18n.LanguageMode

@Composable
fun SettingsRoute(
    modifier: Modifier = Modifier,
    viewModel: SettingsViewModel = viewModel()
) {
    val state = viewModel.uiState.collectAsStateWithLifecycle().value
    val context = LocalContext.current

    LaunchedEffect(viewModel) {
        viewModel.events.collect { message ->
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        }
    }

    FeatureScreenSurface(modifier = modifier) {
        LanguageSection(
            state = state,
            onModeSelected = viewModel::onLanguageModeSelected,
            onLanguageSelected = viewModel::onManualLanguageSelected
        )
        VersionSection(
            state = state,
            onCheckUpdate = viewModel::checkForUpdates,
            onDownloadInstall = viewModel::downloadAndInstallUpdate
        )
    }
}

@Composable
private fun LanguageSection(
    state: SettingsUiState,
    onModeSelected: (LanguageMode) -> Unit,
    onLanguageSelected: (AppLanguage) -> Unit
) {
    SectionCard(
        title = stringResource(R.string.settings_language_title),
        subtitle = stringResource(R.string.settings_language_subtitle)
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            FilterChip(
                selected = state.languageMode == LanguageMode.Auto,
                onClick = { onModeSelected(LanguageMode.Auto) },
                label = { Text(text = stringResource(R.string.settings_language_auto)) }
            )
            FilterChip(
                selected = state.languageMode == LanguageMode.Manual,
                onClick = { onModeSelected(LanguageMode.Manual) },
                label = { Text(text = stringResource(R.string.settings_language_manual)) }
            )
        }
        Text(
            text = stringResource(R.string.settings_language_effective, stringResource(state.effectiveLanguage.labelRes)),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        if (state.languageMode == LanguageMode.Manual) {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                AppLanguage.entries.forEach { language ->
                    FilterChip(
                        selected = state.manualLanguage == language,
                        onClick = { onLanguageSelected(language) },
                        label = { Text(text = stringResource(language.labelRes)) }
                    )
                }
            }
        }
    }
}

@Composable
private fun VersionSection(
    state: SettingsUiState,
    onCheckUpdate: () -> Unit,
    onDownloadInstall: () -> Unit
) {
    SectionCard(
        title = stringResource(R.string.settings_about_title),
        subtitle = stringResource(R.string.settings_about_subtitle)
    ) {
        Text(
            text = stringResource(R.string.settings_version_value, state.versionName, state.versionCode),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
        Button(
            onClick = onCheckUpdate,
            enabled = !state.isCheckingUpdate && !state.isDownloadingUpdate,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = stringResource(R.string.settings_check_update))
        }
        state.updateStatusText?.let { status ->
            Text(
                text = status,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        if (state.isDownloadingUpdate) {
            LinearProgressIndicator(
                progress = { state.downloadProgress / 100f },
                modifier = Modifier.fillMaxWidth()
            )
        }
        state.availableUpdate?.let { updateInfo ->
            OutlinedCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.outlinedCardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
                ),
                border = CardDefaults.outlinedCardBorder()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = updateInfo.releaseTitle.ifBlank { updateInfo.latestVersionName },
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = stringResource(R.string.settings_update_release_time, TimeFormatter.formatIsoDateTime(updateInfo.publishedAt)),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (updateInfo.changelog.isNotBlank()) {
                        Text(
                            text = updateInfo.changelog,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Button(
                        onClick = onDownloadInstall,
                        enabled = !state.isDownloadingUpdate,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(text = stringResource(R.string.settings_download_install))
                    }
                }
            }
        }
    }
}
