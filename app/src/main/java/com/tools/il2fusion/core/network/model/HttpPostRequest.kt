package com.tools.il2fusion.core.network.model

data class HttpPostRequest(
    val url: String,
    val headers: Map<String, String> = emptyMap(),
    val body: String = "",
    val contentType: String = "application/json; charset=utf-8"
)
