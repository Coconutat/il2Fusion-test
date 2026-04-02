package com.tools.il2fusion.core.network.model

data class HttpGetRequest(
    val url: String,
    val headers: Map<String, String> = emptyMap(),
    val queryParameters: Map<String, String> = emptyMap()
)
