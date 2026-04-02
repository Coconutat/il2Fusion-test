package com.tools.il2fusion.core.network.model

data class HttpResponseModel(
    val code: Int,
    val body: String,
    val headers: Map<String, String>,
    val isSuccessful: Boolean
)
