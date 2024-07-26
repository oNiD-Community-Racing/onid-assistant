package io.github.nocomment1105.onidassistant.api

import kotlinx.serialization.Serializable

@Serializable
data class BoostedHosts(
	val hostId: Int,
	val hostName: String,
	val boostCount: Int
)
