package io.github.nocomment1105.onidassistant.database.entities

import kotlinx.serialization.Serializable

/**
 * The metadata for the database.
 *
 * @property version The current version of the database
 * @property id The id of the database, must remain constant
 */
@Serializable
data class MetaData(
	val version: Int,
	val id: String = "meta"
)
