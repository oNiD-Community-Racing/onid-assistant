package io.github.nocomment1105.onidassistant.api

import kotlinx.serialization.Serializable

@Serializable
data class PageInfo(
    val resultsPerPage: Int?,
    val totalResults: Int?
)
