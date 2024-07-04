package io.github.nocomment1105.onidassistant.database.entities

import dev.kord.common.entity.Snowflake
import kotlinx.serialization.Serializable

/**
 * Config Data for the channel creator. Basically values that are set once and called again from a fixed list.
 *
 * @property guildId The ID of the guild these categories are in
 * @property acCategoryId The Assetto Corsa category ID
 * @property accCategoryId The Assetto Corsa Competizione category ID
 * @property ams2CategoryId The Automobilista 2 Category ID
 * @property enduroCategoryId The Enduro event category ID
 * @property consoleCategoryId The Console category ID
 * @property otherSimCategoryId The ID of the category for other sims
 */
@Serializable
data class ChannelCreatorConfigData(
	val guildId: Snowflake,
	val acCategoryId: Snowflake,
	val accCategoryId: Snowflake,
	val ams2CategoryId: Snowflake,
	val enduroCategoryId: Snowflake,
	val consoleCategoryId: Snowflake,
	val otherSimCategoryId: Snowflake,
)
