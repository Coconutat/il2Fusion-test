package com.tools.il2fusion.core.network

import com.tools.il2fusion.core.network.model.NetworkErrorModel

sealed interface NetworkResult<out T> {
    data class Success<T>(val value: T) : NetworkResult<T>
    data class Failure(val error: NetworkErrorModel) : NetworkResult<Nothing>
}
