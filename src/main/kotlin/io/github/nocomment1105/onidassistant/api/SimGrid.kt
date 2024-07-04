package io.github.nocomment1105.onidassistant.api

import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json

internal const val SG_API_VERSION = 1

class SimGrid(private val baseUrl: String = "https://www.thesimgrid.com/api/v$SG_API_VERSION") {
	private val logger = KotlinLogging.logger {}

	private val championshipUrl: String = "${this.baseUrl}/championships"

	private val client = HttpClient {
		install(ContentNegotiation) {
			json(
				Json { ignoreUnknownKeys = true },
				ContentType.Any
			)
		}

		expectSuccess = true
	}

	suspend fun getChampionships(
		racesCount: RacesCount? = null,
		entryFeeRequired: Boolean? = null,
		status: Status? = null,
		driver: DriverTypes? = null,
		grid: GridType? = null
	): Championship {
		val url =
			championshipUrl.plus(
			    "?races_count=${racesCount?.value ?: ""}" +
				"&entry_fee_required=${entryFeeRequired ?: ""}&status=${status?.value ?: ""}" +
				"&driver=${driver?.value ?: ""}&grid=${grid?.value ?: ""}"
			)

		try {
		    val result: Championship = client.get(url).body()
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
