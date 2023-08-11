package io.github.nocomment1105.onidassistant.api

import kotlinx.serialization.Serializable

@Serializable
data class ThumbnailDetails(
    val default: Thumbnail? = null,
    val medium: Thumbnail? = null,
    val high: Thumbnail? = null,
    val standard: Thumbnail? = null,
    val maxres: Thumbnail? = null,
)
