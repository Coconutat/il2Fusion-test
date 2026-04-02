package com.tools.il2fusion.feature.common

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import com.tools.il2fusion.R
import com.tools.il2fusion.core.design.SectionCard

@Composable
fun UsageNoteCard(modifier: Modifier = Modifier) {
    SectionCard(
        title = stringResource(R.string.usage_notes_title),
        modifier = modifier
    ) {
        Text(
            text = stringResource(R.string.usage_notes_body),
            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
