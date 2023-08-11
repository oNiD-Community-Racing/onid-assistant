package io.github.nocomment1105.onidassistant.api

import kotlinx.serialization.Serializable

@Serializable
data class SearchResult(
    val kind: String?,
    val etag: String?,
    val id: ResourceId,
    val snippet: SearchResultSnippet?
)
