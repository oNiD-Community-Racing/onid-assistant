package io.github.nocomment1105.onidassistant.api

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Championship(
	val id: Int,
	val name: String,
	@SerialName("start_date")
	val startDate: String,
	@SerialName("end_date")
	val endDate: String,
	val capacity: Int,
	@SerialName("total_slots_taken")
	val totalSlotsTaken: Int,
	val races: List<Races>,
	@SerialName("teams_enabled")
	val teamsEnabled: Boolean,
	@SerialName("entry_fee_required")
	val entryFeeRequired: Boolean,
	@SerialName("entry_fee_cents")
	val entryFeeCents: Int,
	@SerialName("accepting_registrations")
	val acceptingRegistrations: Boolean,
	val image: String,
	@SerialName("host_name")
	val hostName: String,
	@SerialName("game_name")
	val gameName: String,
	val url: String,
	@SerialName("results_url")
	val resultsUrl: String,
)
