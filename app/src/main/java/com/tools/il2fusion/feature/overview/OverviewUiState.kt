package com.tools.il2fusion.feature.overview

import com.tools.il2fusion.feature.common.HookSummaryUiModel

data class OverviewUiState(
    val isLoading: Boolean = true,
    val summary: HookSummaryUiModel = HookSummaryUiModel()
)
