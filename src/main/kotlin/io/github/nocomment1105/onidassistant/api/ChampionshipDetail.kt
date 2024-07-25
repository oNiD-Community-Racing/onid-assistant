package io.github.nocomment1105.onidassistant.api

import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

@Serializable
data class ChampionshipDetail(
	val id: Int,
	val name: String,
	val startDate: Instant,
	val endDate: String? = null,
	val capacity: Int,
	val totalSlotsTaken: Int? = null,
	val races: List<Races>,
	val teamsEnabled: Boolean,
	val entryFeeRequired: Boolean,
	val entryFeeCents: Int? = null,
	val acceptingRegistrations: Boolean,
	val image: String? = null,
	val hostName: String,
	val gameName: String,
	val url: String,
	val resultsUrl: String,
)
