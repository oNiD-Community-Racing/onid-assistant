package io.github.nocomment1105.onidassistant.api

import dev.kord.common.entity.Snowflake
import kotlinx.serialization.Serializable

@Serializable
data class SGUser(
	val userId: Int,
	val username: String,
	val steam64Id: Snowflake,
	val discordUid: Snowflake,
	val preferredName: String,
	val teams: List<Team>,
	val totalRacesStarted: Int,
	val totalWins: Int,
	val totalPodiums: Int,
	val simgridProActive: Boolean? = null,
	val boostedHosts: List<String>,
	val gridRatings: List<GridRating>,
)
