package com.tools.il2fusion.core.update.model

data class LatestReleaseResponse(
    val tagName: String = "",
    val name: String = "",
    val body: String = "",
    val publishedAt: String = "",
    val htmlUrl: String = "",
    val assets: List<ReleaseAssetResponse> = emptyList(),
    val message: String? = null,
    val documentationUrl: String? = null
)
