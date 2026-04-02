package com.tools.il2fusion.core.update.model

data class CheckUpdateRequest(
    val currentVersionName: String,
    val currentVersionCode: Int
)
