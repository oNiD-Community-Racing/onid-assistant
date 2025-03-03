package io.github.nocomment1105.onidassistant.extensions

import dev.kordex.core.extensions.Extension
import dev.kordex.core.extensions.publicSlashCommand
import dev.kordex.core.i18n.toKey

class PasswordCommands : Extension() {
	override val name: String = "password-commands"

	override suspend fun setup() {
		publicSlashCommand {
			name = "password".toKey()
			description = "For the moment when *someone* asks about the password".toKey()

			action {
				respond {
					content = "It's on SimGrid!"
				}
			}
		}
	}
}
