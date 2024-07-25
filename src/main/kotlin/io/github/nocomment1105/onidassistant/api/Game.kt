package io.github.nocomment1105.onidassistant.api

import kotlinx.serialization.Serializable

@Serializable
data class Game(
	val id: Int,
	val name: String,
	val gameType: String,
	val abbreviation: String,
)
