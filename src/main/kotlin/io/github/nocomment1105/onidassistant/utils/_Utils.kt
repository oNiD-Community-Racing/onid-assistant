package io.github.nocomment1105.onidassistant.utils

import dev.kordex.core.builders.ExtensibleBotBuilder
import dev.kordex.core.utils.loadModule
import io.github.nocomment1105.onidassistant.database.Database
import io.github.nocomment1105.onidassistant.database.collections.ChannelCreatorConfigCollection
import io.github.nocomment1105.onidassistant.database.collections.MetaCollection
import io.github.oshai.kotlinlogging.KLogger
import io.ktor.client.plugins.*
import io.ktor.http.*
import kotlinx.coroutines.runBlocking
import org.koin.dsl.bind

suspend inline fun ExtensibleBotBuilder.database(migrate: Boolean) {
	val db = Database()

	hooks {
		beforeKoinSetup {
			loadModule {
				single { db } bind Database::class
			}

			loadModule {
				single { MetaCollection() } bind MetaCollection::class
				single { ChannelCreatorConfigCollection() } bind ChannelCreatorConfigCollection::class
			}

			if (migrate) {
				runBlocking {
					db.migrate()
				}
			}
		}
	}
}

/**
 * A utility function to handle responding to errors. Currently only supports Client request exceptions
 *
 * @param logger The [KLogger] to send the logging information too
 * @param e The exception that was thrown
 */
fun errorResponse(logger: KLogger, e: Exception) {
	if (e is ClientRequestException && e.response.status.value in 400 until 600) {
		if (e.response.status.value == HttpStatusCode.NotFound.value) {
			logger.debug { "Code ${e.response.status}" }
		} else {
			logger.error(e) { "Code ${e.response.status}" }
		}
	}
}
