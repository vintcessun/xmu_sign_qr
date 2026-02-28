package org.qrcodedemo.app.api

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import org.qrcodedemo.app.data.MessageResponse
import org.qrcodedemo.app.data.QrSignRequest
import org.qrcodedemo.app.data.QrSignResponse

object ApiService {
    private val client = HttpClient {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                prettyPrint = true
                isLenient = true
            })
        }
        install(Logging) {
            level = LogLevel.ALL
        }
    }

    private const val BASE_URL = "https://zzy.vintces.icu"

    suspend fun signQr(content: String): List<QrSignResponse> {
        val response: MessageResponse<List<QrSignResponse>> = client.post("$BASE_URL/rollcall/qr") {
            contentType(ContentType.Application.Json)
            setBody(QrSignRequest(content))
        }.body()
        
        return response.message
    }
}
