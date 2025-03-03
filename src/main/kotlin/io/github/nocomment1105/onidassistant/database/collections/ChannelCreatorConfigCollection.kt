package io.github.nocomment1105.onidassistant.database.collections

import dev.kord.common.entity.Snowflake
import dev.kordex.core.koin.KordExKoinComponent
import io.github.nocomment1105.onidassistant.database.Database
import io.github.nocomment1105.onidassistant.database.entities.ChannelCreatorConfigData
import io.github.nocomment1105.onidassistant.utils.CategoryType
import org.koin.core.component.inject
import org.litote.kmongo.eq

class ChannelCreatorConfigCollection : KordExKoinComponent {
	private val db: Database by inject()

	@PublishedApi
	internal val collection = db.onidDb.getCollection<ChannelCreatorConfigData>()

	suspend inline fun getCategoryId(inputGuildId: Snowflake, category: CategoryType): Snowflake? {
		val coll = collection.findOne(ChannelCreatorConfigData::guildId eq inputGuildId)
		return when (category) {
			CategoryType.AC -> coll?.acCategoryId
			CategoryType.ACC -> coll?.accCategoryId
			CategoryType.AMS2 -> coll?.ams2CategoryId
			CategoryType.LMU -> coll?.lmuCategoryId
			CategoryType.ENDURO -> coll?.enduroCategoryId
			CategoryType.CONSOLE -> coll?.consoleCategoryId
			CategoryType.OTHER -> coll?.otherSimCategoryId
		}
	}

	suspend inline fun setConfig(
		inputGuildID: Snowflake,
		acCategoryId: Snowflake,
		accCategoryId: Snowflake,
		ams2CategoryId: Snowflake,
		lmuCategoryId: Snowflake,
		enduroCategoryId: Snowflake,
		consoleCategoryId: Snowflake,
		otherCategoryId: Snowflake,
	) {
		collection.deleteOne(ChannelCreatorConfigData::guildId eq inputGuildID)
		collection.insertOne(
			ChannelCreatorConfigData(
				inputGuildID,
				acCategoryId,
				accCategoryId,
				ams2CategoryId,
				lmuCategoryId,
				enduroCategoryId,
				consoleCategoryId,
				otherCategoryId
			)
		)
	}

	suspend inline fun clearConfig(inputGuildID: Snowflake) =
		collection.deleteOne(ChannelCreatorConfigData::guildId eq inputGuildID)
}
