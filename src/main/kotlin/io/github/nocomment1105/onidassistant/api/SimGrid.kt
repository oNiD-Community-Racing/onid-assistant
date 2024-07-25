package io.github.nocomment1105.onidassistant.api

import dev.kord.common.entity.Snowflake
import io.github.nocomment1105.onidassistant.utils.SG_API_KEY
import io.github.nocomment1105.onidassistant.utils.errorResponse
import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.auth.*
import io.ktor.client.plugins.auth.providers.BearerTokens
import io.ktor.client.plugins.auth.providers.bearer
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonNamingStrategy

/**
 * The version of the SimGrid API in use.
 */
internal const val SG_API_VERSION = 1

@OptIn(ExperimentalSerializationApi::class)
class SimGrid(private val baseUrl: String = "https://www.thesimgrid.com/api/v$SG_API_VERSION") {
	private val logger = KotlinLogging.logger {}

	private val client = HttpClient {
		install(ContentNegotiation) {
			json(
				Json {
					ignoreUnknownKeys = true
					namingStrategy = JsonNamingStrategy.SnakeCase // Avoids the need for serial name annotations in the data classes
				},
				ContentType.Any
			)
		}

		install(Auth) {
			bearer {
				loadTokens {
					// We only have one token so the refresh token is the same as the regular access token
					BearerTokens(SG_API_KEY, SG_API_KEY)
				}
			}
		}

		expectSuccess = true
	}

	/**
	 * Gets all the championships for this host.
	 *
	 * @return A List of [Championship]s
	 */
	suspend fun getChampionships(): List<Championship> {
		try {
		    val result: List<Championship> = client.get("$baseUrl/championships").body()
			logger.debug { "getChampionships: Code 200" }

			return result
		} catch (e: ClientRequestException) {
			errorResponse(logger, e)

			throw e
		}
	}

	/**
	 * Get more in-depth details about a specific [Championship].
	 *
	 * @param id the ID of the championship to get the details for
	 * @return The [ChampionshipDetail]s for the given championship ID
	 */
	suspend fun getChampionshipDetails(id: Int): ChampionshipDetail {
		val url = "$baseUrl/championships/$id"
		try {
			val result: ChampionshipDetail = client.get(url).body()
			logger.debug { "getChampionshipDetails($id): Code 200" }
			return result
		} catch (e: ClientRequestException) {
			errorResponse(logger, e)
			throw e
		}
	}

	/**
	 * Get the indepth details of all championships for the host.
	 *
	 * @return A List of [ChampionshipDetail]s for all the championships
	 */
	suspend fun getDetailedChampionships(): List<ChampionshipDetail> {
		val championships = getChampionships()
		val returnList = mutableListOf<ChampionshipDetail>()
		championships.forEach {
			returnList.add(getChampionshipDetails(it.id))
		}
		return returnList
	}

	/**
	 * Get the data for a given [SimGrid user][SGUser] via their discord id.
	 *
	 * @param id The Discord ID of the user to get
	 */
	suspend fun getUser(id: Snowflake): SGUser {
		val url = "$baseUrl/users/$id?attribute=discord"
		try {
			val result: SGUser = client.get(url).body()
			logger.debug { "getUser($id): Code 200" }
			return result
		} catch (e: ClientRequestException) {
			errorResponse(logger, e)
			throw e
		}
	}

	/**
	 * Internal function to get the list of games supported by SimGrid.
	 *
	 * @return A list of [Game] details
	 */
	private suspend fun getGames(): List<Game> {
		val url = "$baseUrl/games"
		try {
			val result: List<Game> = client.get(url).body()
			logger.debug { "getGames: Code 200" }
			return result
		} catch (e: ClientRequestException) {
			errorResponse(logger, e)
			throw e
		}
	}

	/**
	 * Searches for a given [Game] based on its id.
	 *
	 * @see getGames
	 * @return A [Game] object for the given game
	 */
	suspend fun getGameById(id: Int): Game? = getGames().find { it.id == id }
}
