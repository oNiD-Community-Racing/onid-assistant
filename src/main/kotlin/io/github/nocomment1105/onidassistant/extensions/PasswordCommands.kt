package io.github.nocomment1105.onidassistant.extensions

import com.kotlindiscord.kord.extensions.extensions.Extension
import com.kotlindiscord.kord.extensions.extensions.publicSlashCommand

class PasswordCommands : Extension() {
	override val name: String = "password-commands"

	override suspend fun setup() {
		publicSlashCommand {
			name = "password"
			description = "For the moment when *someone* asks about the password"

			action {
				respond {
					content = "It's on SimGrid!"
				}
			}
		}
	}
}
