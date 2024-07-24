package io.github.nocomment1105.onidassistant.database.migrations

import org.litote.kmongo.coroutine.CoroutineDatabase

suspend fun v1(db: CoroutineDatabase) {
	db.createCollection("channelCreatorConfigData")
}
