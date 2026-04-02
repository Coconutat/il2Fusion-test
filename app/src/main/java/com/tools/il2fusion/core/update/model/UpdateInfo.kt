package com.tools.il2fusion.core.update.model

data class UpdateInfo(
    val currentVersionName: String,
    val latestVersionName: String,
    val releaseTitle: String,
    val changelog: String,
    val publishedAt: String,
    val releaseUrl: String,
    val asset: UpdateAsset?,
    val isUpdateAvailable: Boolean
)

data class UpdateAsset(
    val name: String,
    val contentType: String,
    val sizeBytes: Long,
    val downloadUrl: String,
    val digest: String?
)
