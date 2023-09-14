package io.github.nocomment1105.onidassistant.api

import com.google.api.services.youtube.model.TokenPagination
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable

@Serializable
data class SearchListResponse(
	val kind: String?,
	val etag: String?,
	val regionCode: String?,
	val nextPageToken: String? = null,
	val prevPageToken: String? = null,
	val pageInfo: PageInfo,
	val items: List<SearchResult?>?,
	val eventId: String? = null,
	@Contextual val tokenPagination: TokenPagination? = null,
	val visitorId: String? = null
)
