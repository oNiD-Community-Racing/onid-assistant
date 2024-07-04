package io.github.nocomment1105.onidassistant.api

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Races(
	val id: Int,
	@SerialName("race_name")
	val raceName: String,
	val track: String,
	@SerialName("starts_at")
	val startsAt: String
)
