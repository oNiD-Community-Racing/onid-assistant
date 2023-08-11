package io.github.nocomment1105.onidassistant.api

import io.github.nocomment1105.onidassistant.utils.GOOGLE_API_KEY
import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json

internal const val YOUTUBE_API_VERSION = 3

class Youtube(private val baseUrl: String = "https://www.googleapis.com/youtube") {
    private val logger = KotlinLogging.logger { }

    private val searchUrl: String = "${this.baseUrl}/v$YOUTUBE_API_VERSION/search"

    private val client = HttpClient {
        install(ContentNegotiation) {
            json(
                Json { ignoreUnknownKeys = true },
                ContentType.Any
            )
        }

        expectSuccess = true
    }

    suspend fun getCurrentLiveStreamsForChannel(channelId: String): SearchListResponse {
        val url = searchUrl.plus("?part=snippet&channelId=$channelId&eventType=live&type=video&key=$GOOGLE_API_KEY")

        try {
            val result: SearchListResponse = client.get(url).body()

            logger.debug { "Code 200" }

            return result
        } catch (e: ClientRequestException) {
            if (e.response.status.value in 400 until 600) {
                if (e.response.status.value == HttpStatusCode.NotFound.value) {
                    logger.debug { "Code ${e.response.status}" }
                } else {
                    logger.error(e) { "Code ${e.response.status}" }
                }
            }

            throw e
        }
    }
}
