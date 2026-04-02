package com.tools.il2fusion.core.update

import android.content.Context
import com.tools.il2fusion.core.update.model.UpdateAsset
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File

sealed interface ApkDownloadResult {
    data class Success(val file: File) : ApkDownloadResult
    data class Failure(val message: String) : ApkDownloadResult
}

class ApkDownloadManager(
    context: Context,
    private val okHttpClient: OkHttpClient = OkHttpClient()
) {
    private val appContext = context.applicationContext

    suspend fun downloadApk(
        asset: UpdateAsset,
        onProgress: (Int) -> Unit = {}
    ): ApkDownloadResult = withContext(Dispatchers.IO) {
        val request = Request.Builder()
            .url(asset.downloadUrl)
            .header("Accept", "application/octet-stream")
            .header("User-Agent", "il2Fusion-Android")
            .build()

        try {
            okHttpClient.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    return@withContext ApkDownloadResult.Failure(
                        response.message.ifBlank { "HTTP ${response.code}" }
                    )
                }
                val body = response.body ?: return@withContext ApkDownloadResult.Failure("Empty download body")
                val outputDirectory = File(appContext.cacheDir, "updates").apply { mkdirs() }
                val outputFile = File(outputDirectory, asset.name)
                body.byteStream().use { inputStream ->
                    outputFile.outputStream().use { outputStream ->
                        val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
                        var bytesCopied = 0L
                        val totalBytes = body.contentLength()
                        while (true) {
                            val read = inputStream.read(buffer)
                            if (read == -1) break
                            outputStream.write(buffer, 0, read)
                            bytesCopied += read
                            if (totalBytes > 0) {
                                val progress = ((bytesCopied * 100) / totalBytes).toInt().coerceIn(0, 100)
                                onProgress(progress)
                            }
                        }
                        outputStream.flush()
                    }
                }
                onProgress(100)
                ApkDownloadResult.Success(outputFile)
            }
        } catch (throwable: Throwable) {
            ApkDownloadResult.Failure(throwable.message ?: "Failed to download update")
        }
    }
}
