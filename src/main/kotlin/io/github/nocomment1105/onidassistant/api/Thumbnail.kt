package io.github.nocomment1105.onidassistant.api

import kotlinx.serialization.Serializable

@Serializable
data class Thumbnail(
	val url: String?,
	val height: Long?,
	val width: Long?,
)
