package io.github.nocomment1105.onidassistant.api

import kotlinx.serialization.Serializable

@Serializable
data class Team(
	val teamId: Int? = null,
	val name: String,
	val totalRacesStarted: Int? = null,
	val totalWins: Int? = null,
	val totalPodiums: Int? = null,
	val totalPenaltyRate: Float? = null
)
