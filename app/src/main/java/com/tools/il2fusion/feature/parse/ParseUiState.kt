package com.tools.il2fusion.feature.parse

data class ParseUiState(
    val isLoading: Boolean = false,
    val methods: List<String> = emptyList(),
    val savedCount: Int = 0,
    val hasTargetsJson: Boolean = false
)
