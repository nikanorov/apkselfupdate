package com.nikanorov.apkselfupdate.internal

import com.nikanorov.apkselfupdate.util.NetworkState
import com.nikanorov.apkselfupdate.util.flowRequest
import com.nikanorov.apkselfupdate.value.APKSelfUpdateInfoResponse
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

internal class API(private val url: String) {
    private val client = HttpClient(CIO) {
        expectSuccess = true

        install(ContentNegotiation) {
            json(Json {
                prettyPrint = true
                isLenient = true
                ignoreUnknownKeys = true
            })
        }
    }

    fun updateInfo() = flowRequest {
        emit(NetworkState.Loading)
        val response = client.get(url)
        val updateInfo: APKSelfUpdateInfoResponse = response.body()
        emit(NetworkState.Success(updateInfo.updates.firstOrNull()))
    }

}