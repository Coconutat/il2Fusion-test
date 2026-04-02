package com.tools.il2fusion.core.update

import com.tools.il2fusion.core.network.NetworkClient
import com.tools.il2fusion.core.network.NetworkResult
import com.tools.il2fusion.core.network.OkHttpNetworkClient
import com.tools.il2fusion.core.network.api.GithubApi
import com.tools.il2fusion.core.update.model.CheckUpdateRequest
import com.tools.il2fusion.core.update.model.LatestReleaseResponse
import com.tools.il2fusion.core.update.model.ReleaseAssetResponse
import com.tools.il2fusion.core.update.model.UpdateAsset
import com.tools.il2fusion.core.update.model.UpdateCheckResult
import com.tools.il2fusion.core.update.model.UpdateInfo
import org.json.JSONArray
import org.json.JSONObject

class UpdateRepository(
    private val networkClient: NetworkClient = OkHttpNetworkClient()
) {
    suspend fun checkForUpdate(request: CheckUpdateRequest): UpdateCheckResult {
        return when (val result = networkClient.get(GithubApi.latestReleaseRequest())) {
            is NetworkResult.Failure -> UpdateCheckResult.Failure(
                message = result.error.message,
                isRateLimited = result.error.code == 403 && result.error.body?.contains("rate limit", ignoreCase = true) == true
            )

            is NetworkResult.Success -> parseRelease(
                body = result.value.body,
                currentVersionName = request.currentVersionName
            )
        }
    }

    private fun parseRelease(
        body: String,
        currentVersionName: String
    ): UpdateCheckResult {
        return try {
            val response = body.toLatestReleaseResponse()
            if (!response.message.isNullOrBlank() && response.tagName.isBlank()) {
                val isRateLimited = response.message.contains("rate limit", ignoreCase = true)
                UpdateCheckResult.Failure(
                    message = response.message,
                    isRateLimited = isRateLimited
                )
            } else {
                val latestVersionName = normalizeVersion(response.tagName.ifBlank { response.name })
                val updateInfo = UpdateInfo(
                    currentVersionName = normalizeVersion(currentVersionName),
                    latestVersionName = latestVersionName,
                    releaseTitle = response.name.ifBlank { response.tagName },
                    changelog = response.body,
                    publishedAt = response.publishedAt,
                    releaseUrl = response.htmlUrl,
                    asset = response.assets.firstNotNullOfOrNull { it.toUpdateAssetOrNull() },
                    isUpdateAvailable = compareVersions(latestVersionName, currentVersionName) > 0
                )
                if (updateInfo.isUpdateAvailable) {
                    UpdateCheckResult.Available(updateInfo)
                } else {
                    UpdateCheckResult.UpToDate(updateInfo)
                }
            }
        } catch (throwable: Throwable) {
            UpdateCheckResult.Failure(message = throwable.message ?: "Failed to parse update response")
        }
    }

    private fun ReleaseAssetResponse.toUpdateAssetOrNull(): UpdateAsset? {
        val isApk = name.endsWith(".apk", ignoreCase = true) ||
            contentType.equals("application/vnd.android.package-archive", ignoreCase = true)
        if (!isApk || browserDownloadUrl.isBlank()) return null
        return UpdateAsset(
            name = name,
            contentType = contentType,
            sizeBytes = size,
            downloadUrl = browserDownloadUrl,
            digest = digest
        )
    }

    private fun normalizeVersion(value: String): String {
        return value.trim().removePrefix("v")
    }

    private fun compareVersions(remote: String, local: String): Int {
        val remoteParts = remote.versionParts()
        val localParts = local.versionParts()
        val size = maxOf(remoteParts.size, localParts.size)
        for (index in 0 until size) {
            val remotePart = remoteParts.getOrElse(index) { 0 }
            val localPart = localParts.getOrElse(index) { 0 }
            if (remotePart != localPart) {
                return remotePart.compareTo(localPart)
            }
        }
        return 0
    }

    private fun String.versionParts(): List<Int> {
        return removePrefix("v")
            .split(Regex("[^0-9]+"))
            .filter { it.isNotBlank() }
            .map { it.toIntOrNull() ?: 0 }
            .ifEmpty { listOf(0) }
    }

    private fun String.toLatestReleaseResponse(): LatestReleaseResponse {
        val root = JSONObject(this)
        val assets = root.optJSONArray("assets").toReleaseAssets()
        return LatestReleaseResponse(
            tagName = root.optString("tag_name"),
            name = root.optString("name"),
            body = root.optString("body"),
            publishedAt = root.optString("published_at"),
            htmlUrl = root.optString("html_url"),
            assets = assets,
            message = root.optString("message").ifBlank { null },
            documentationUrl = root.optString("documentation_url").ifBlank { null }
        )
    }

    private fun JSONArray?.toReleaseAssets(): List<ReleaseAssetResponse> {
        if (this == null) return emptyList()
        return buildList(length()) {
            for (index in 0 until length()) {
                val item = optJSONObject(index) ?: continue
                add(
                    ReleaseAssetResponse(
                        name = item.optString("name"),
                        contentType = item.optString("content_type"),
                        size = item.optLong("size"),
                        digest = item.optString("digest").ifBlank { null },
                        browserDownloadUrl = item.optString("browser_download_url")
                    )
                )
            }
        }
    }
}
