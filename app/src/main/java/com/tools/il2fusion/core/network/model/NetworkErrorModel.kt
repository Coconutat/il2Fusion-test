package com.tools.il2fusion.core.network.model

data class NetworkErrorModel(
    val code: Int? = null,
    val message: String,
    val body: String? = null
)
