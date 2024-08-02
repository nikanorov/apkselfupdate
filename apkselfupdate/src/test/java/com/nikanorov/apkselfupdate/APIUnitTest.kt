package com.nikanorov.apkselfupdate

import com.nikanorov.apkselfupdate.internal.API
import com.nikanorov.apkselfupdate.util.firstSuccessResponse
import com.nikanorov.apkselfupdate.value.APKSelfUpdateInfoUpdate
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import io.ktor.utils.io.ByteReadChannel
import kotlinx.coroutines.runBlocking
import org.junit.Assert
import org.junit.Test

class APIUnitTest {

    @Test
    fun receiveParsedResponseTest() {
        runBlocking {
            val mockEngine = MockEngine { _ ->
                respond(
                    content = ByteReadChannel(
                        "{\n" +
                            "  \"updates\": [\n" +
                            "    {\n" +
                            "      \"versionCode\": 2,\n" +
                            "      \"versionNumber\": \"1.0.1\",\n" +
                            "      \"changelog\": \"Bug fix\",\n" +
                            "      \"apkUrl\": \"https://example.com/latest.apk\"\n" +
                            "    }\n" +
                            "  ]\n" +
                            "}"
                    ),
                    status = HttpStatusCode.OK,
                    headers = headersOf(HttpHeaders.ContentType, "application/json")
                )
            }
            val apiClient = API("http://localhost/update.json", mockEngine)

            Assert.assertEquals(
                 APKSelfUpdateInfoUpdate(
                        versionCode = 2,
                        versionNumber = "1.0.1",
                        changelog = "Bug fix",
                        apkUrl = "https://example.com/latest.apk"
                ), apiClient.updateInfo().firstSuccessResponse()?.data
            )
        }
    }
}
