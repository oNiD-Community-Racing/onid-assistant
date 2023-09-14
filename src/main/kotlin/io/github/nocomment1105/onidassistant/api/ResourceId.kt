package io.github.nocomment1105.onidassistant.api

import kotlinx.serialization.Serializable

@Serializable
data class ResourceId(
	val kind: String? = null,
	val videoId: String? = null,
	val channelId: String? = null,
	val playlistId: String? = null,
)
