package com.nikanorov.apkselfupdate.internal

import android.content.Context
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.cio.CIO
import io.ktor.client.request.prepareGet
import io.ktor.http.contentLength
import io.ktor.utils.io.ByteReadChannel
import io.ktor.utils.io.core.isEmpty
import io.ktor.utils.io.core.readBytes
import java.io.File

internal class Downloader (private val context: Context) {
    private val client = HttpClient(CIO) {
        expectSuccess = true
    }

    suspend fun downloadFile(
        urlString: String,
        percentUpdateCallback: (Int?) -> Unit,
        downloadCompletedCallback: (String) -> Unit
    ) {
        client.prepareGet(urlString).execute { httpResponse ->
            val file = File(context.cacheDir, UPDATE_FILE_NAME)
            if (file.isFile && file.exists())
                file.delete()
            val channel: ByteReadChannel = httpResponse.body()
            while (!channel.isClosedForRead) {
                val packet = channel.readRemaining(DEFAULT_BUFFER_SIZE.toLong())
                while (!packet.isEmpty) {
                    val bytes = packet.readBytes()
                    file.appendBytes(bytes)
                    val contentLength = httpResponse.contentLength()
                    val fileLength = file.length()

                    contentLength?.let {
                        percentUpdateCallback ((fileLength.toDouble() / it * 100).toInt())
                    }
                }
            }
            downloadCompletedCallback(file.path)
        }
    }

    companion object {
        const val UPDATE_FILE_NAME = "update.apk"
    }

}