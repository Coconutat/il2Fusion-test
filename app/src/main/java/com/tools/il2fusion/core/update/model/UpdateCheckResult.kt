package com.tools.il2fusion.core.update.model

sealed interface UpdateCheckResult {
    data class Available(val info: UpdateInfo) : UpdateCheckResult
    data class UpToDate(val info: UpdateInfo) : UpdateCheckResult
    data class Failure(
        val message: String,
        val isRateLimited: Boolean = false
    ) : UpdateCheckResult
}
