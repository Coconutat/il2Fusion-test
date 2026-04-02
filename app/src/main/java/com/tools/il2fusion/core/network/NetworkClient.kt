package com.tools.il2fusion.core.network

import com.tools.il2fusion.core.network.model.HttpGetRequest
import com.tools.il2fusion.core.network.model.HttpPostRequest
import com.tools.il2fusion.core.network.model.HttpResponseModel

interface NetworkClient {
    suspend fun get(request: HttpGetRequest): NetworkResult<HttpResponseModel>
    suspend fun post(request: HttpPostRequest): NetworkResult<HttpResponseModel>
}
