package com.tools.il2fusion.core.network.api

import com.tools.il2fusion.core.network.model.HttpGetRequest

object GithubApi {
    private const val USER_AGENT = "il2Fusion-Android"
    const val LatestReleaseUrl = "https://api.github.com/repos/PenguinAndy/il2Fusion/releases/latest"

    fun latestReleaseRequest(): HttpGetRequest {
        return HttpGetRequest(
            url = LatestReleaseUrl,
            headers = mapOf(
                "Accept" to "application/vnd.github+json",
                "User-Agent" to USER_AGENT,
                "X-GitHub-Api-Version" to "2022-11-28"
            )
        )
    }
}
