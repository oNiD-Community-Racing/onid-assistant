@file:Suppress("UseIfInsteadOfWhen")

/*
 * This source code form has been adapted from QuiltMC's Discord Bot, Cozy (https://github.com/QuiltMC/cozy-discord).
 *
 * As a result of this, the following license applies to this source code:
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package io.github.nocomment1105.onidassistant.database.migrations

import dev.kordex.core.koin.KordExKoinComponent
import io.github.nocomment1105.onidassistant.database.Database
import io.github.nocomment1105.onidassistant.database.collections.MetaCollection
import io.github.nocomment1105.onidassistant.database.entities.MetaData
import io.github.oshai.kotlinlogging.KotlinLogging
import org.koin.core.component.inject

object Migrator : KordExKoinComponent {
	private val logger = KotlinLogging.logger { "Migration Logger" }

	val db: Database by inject()
	private val metaCollection: MetaCollection by inject()

	suspend fun migrate() {
		logger.info { "Starting database migration" }

		var meta = metaCollection.get()

		if (meta == null) {
			meta = MetaData(0)
			metaCollection.set(meta)
		}

		var currentVersion = meta.version
		logger.info { "Current database version: v$currentVersion" }

		while (true) {
			val nextVersion = currentVersion + 1

			@Suppress("TooGenericExceptionCaught")
			try {
				when (nextVersion) {
					1 -> ::v1
					else -> break
				}(db.onidDb)

				logger.info { "Migrated database to version $nextVersion" }
			} catch (t: Throwable) {
				logger.error(t) { "Failed to migrate database to version $nextVersion" }
				throw t
			}

			currentVersion = nextVersion
		}

		if (currentVersion != meta.version) {
			meta = meta.copy(version = currentVersion)

			metaCollection.update(meta)

			logger.info { "Finished database migrations." }
		}
	}
}
