package io.github.nocomment1105.onidassistant.api

import kotlinx.serialization.Serializable

@Serializable
data class SearchResultSnippet(
    val publishedAt: String?,
    val channelId: String?,
    val title: String?,
    val description: String?,
    val thumbnails: ThumbnailDetails?,
    val channelTitle: String?,
    val liveBroadcastContent: String?,
)
