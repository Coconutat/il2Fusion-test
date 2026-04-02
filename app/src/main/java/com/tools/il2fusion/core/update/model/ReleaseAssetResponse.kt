package com.tools.il2fusion.core.update.model

data class ReleaseAssetResponse(
    val name: String = "",
    val contentType: String = "",
    val size: Long = 0L,
    val digest: String? = null,
    val browserDownloadUrl: String = ""
)
