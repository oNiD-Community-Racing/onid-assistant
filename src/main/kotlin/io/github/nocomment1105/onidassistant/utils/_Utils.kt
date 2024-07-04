package io.github.nocomment1105.onidassistant.utils

import com.kotlindiscord.kord.extensions.builders.ExtensibleBotBuilder
import com.kotlindiscord.kord.extensions.utils.loadModule
import io.github.nocomment1105.onidassistant.database.Database
import io.github.nocomment1105.onidassistant.database.collections.ChannelCreatorConfigCollection
import io.github.nocomment1105.onidassistant.database.collections.MetaCollection
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
