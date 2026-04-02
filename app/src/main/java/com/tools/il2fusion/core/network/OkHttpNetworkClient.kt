package com.tools.il2fusion.core.network

import com.tools.il2fusion.core.network.model.HttpGetRequest
import com.tools.il2fusion.core.network.model.HttpPostRequest
import com.tools.il2fusion.core.network.model.HttpResponseModel
import com.tools.il2fusion.core.network.model.NetworkErrorModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.RequestBody
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull

class OkHttpNetworkClient(
    private val okHttpClient: OkHttpClient = OkHttpClient()
) : NetworkClient {

    override suspend fun get(request: HttpGetRequest): NetworkResult<HttpResponseModel> {
        val url = request.url.toHttpUrlOrNull()?.newBuilder()?.apply {
            request.queryParameters.forEach { (key, value) ->
                addQueryParameter(key, value)
            }
        }?.build()
            ?: return NetworkResult.Failure(NetworkErrorModel(message = "Invalid GET URL"))

        val okRequest = Request.Builder()
            .url(url)
            .get()
            .applyHeaders(request.headers)
            .build()
        return execute(okRequest)
    }

    override suspend fun post(request: HttpPostRequest): NetworkResult<HttpResponseModel> {
        val url = request.url.toHttpUrlOrNull()
            ?: return NetworkResult.Failure(NetworkErrorModel(message = "Invalid POST URL"))
        val body: RequestBody = request.body.toRequestBody(request.contentType.toMediaType())
        val okRequest = Request.Builder()
            .url(url)
            .post(body)
            .applyHeaders(request.headers)
            .build()
        return execute(okRequest)
    }

    private suspend fun execute(request: Request): NetworkResult<HttpResponseModel> = withContext(Dispatchers.IO) {
        try {
            okHttpClient.newCall(request).execute().use { response ->
                val responseModel = HttpResponseModel(
                    code = response.code,
                    body = response.body?.string().orEmpty(),
                    headers = response.headers.toMultimap().mapValues { it.value.joinToString(",") },
                    isSuccessful = response.isSuccessful
                )
                if (response.isSuccessful) {
                    NetworkResult.Success(responseModel)
                } else {
                    NetworkResult.Failure(
                        NetworkErrorModel(
                            code = response.code,
                            message = response.message.ifBlank { "HTTP ${response.code}" },
                            body = responseModel.body
                        )
                    )
                }
            }
        } catch (throwable: Throwable) {
            NetworkResult.Failure(
                NetworkErrorModel(message = throwable.message ?: "Unknown network error")
            )
        }
    }

    private fun Request.Builder.applyHeaders(headers: Map<String, String>): Request.Builder {
        headers.forEach { (key, value) -> header(key, value) }
        return this
    }
}
