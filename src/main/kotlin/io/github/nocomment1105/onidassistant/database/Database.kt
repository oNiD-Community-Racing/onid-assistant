package io.github.nocomment1105.onidassistant.database

import com.mongodb.ConnectionString
import com.mongodb.MongoClientSettings
import io.github.nocomment1105.onidassistant.database.migrations.Migrator
import io.github.nocomment1105.onidassistant.utils.MONGO_URI
import org.bson.UuidRepresentation
import org.litote.kmongo.coroutine.coroutine
import org.litote.kmongo.reactivestreams.KMongo

class Database {
	private val settings = MongoClientSettings
		.builder()
		.uuidRepresentation(UuidRepresentation.STANDARD)
		.applyConnectionString(ConnectionString(MONGO_URI))
		.build()

	private val client = KMongo.createClient(settings).coroutine

	val onidDb get() = client.getDatabase("oNiDBot")

	suspend fun migrate() {
		Migrator.migrate()
	}
}
