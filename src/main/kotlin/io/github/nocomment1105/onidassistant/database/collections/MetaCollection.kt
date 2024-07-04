package io.github.nocomment1105.onidassistant.database.collections

import com.kotlindiscord.kord.extensions.koin.KordExKoinComponent
import io.github.nocomment1105.onidassistant.database.Database
import io.github.nocomment1105.onidassistant.database.entities.MetaData
import org.koin.core.component.inject
import org.litote.kmongo.eq

class MetaCollection : KordExKoinComponent {
	private val db: Database by inject()

	@PublishedApi
	internal val collection = db.onidDb.getCollection<MetaData>()

	/**
	 * Gets the metadata from the database.
	 *
	 * @return A [MetaData] object of the data
	 */
	suspend inline fun get(): MetaData? = collection.findOne()

	/**
	 * Sets the metadata when the table is first created.
	 */
	suspend inline fun set(meta: MetaData) = collection.insertOne(meta)

	/**
	 * Updates the metadata in the database with new [meta].
	 */
	suspend inline fun update(meta: MetaData) = collection.findOneAndReplace(MetaData::id eq "meta", meta)
}
