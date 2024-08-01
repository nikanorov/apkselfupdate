package com.nikanorov.apkselfupdate.util

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn


internal sealed class NetworkState<out T> {
    data class Success<out T>(val data: T) : NetworkState<T>()
    data class Error(val message: String?, val error: Throwable?) : NetworkState<Nothing>()
    data object Idle : NetworkState<Nothing>()
    data object Loading : NetworkState<Nothing>()
}

internal fun <T> flowRequest(block: suspend FlowCollector<NetworkState<T>>.() -> Unit): Flow<NetworkState<T>> =
    flow(block).catch { e ->
        emit(NetworkState.Error(e.message, e))
    }.flowOn(Dispatchers.IO)

internal suspend fun <T> Flow<NetworkState<T>>.firstSuccessResponse(): NetworkState.Success<T>? {
    return this.filterIsInstance<NetworkState.Success<T>>().firstOrNull()
}