package io.github.nocomment1105.onidassistant.database.migrations

import dev.kord.common.entity.Snowflake
import io.github.nocomment1105.onidassistant.database.collections.ChannelCreatorConfigCollection
import io.github.nocomment1105.onidassistant.database.entities.ChannelCreatorConfigData
import org.litote.kmongo.coroutine.CoroutineDatabase
import org.litote.kmongo.exists
import org.litote.kmongo.setValue

suspend fun v2(db: CoroutineDatabase) {
	with(db.getCollection<ChannelCreatorConfigCollection>()) {
		updateMany(
			ChannelCreatorConfigData::lmuCategoryId exists false,
			setValue(ChannelCreatorConfigData::lmuCategoryId, Snowflake(0))
		)
	}
}
