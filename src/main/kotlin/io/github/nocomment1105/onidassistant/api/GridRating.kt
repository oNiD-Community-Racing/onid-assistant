package io.github.nocomment1105.onidassistant.api

import kotlinx.serialization.Serializable

@Serializable
data class GridRating(
	val gameId: Int,
	val rating: Int,
	val preferred: Boolean
)
