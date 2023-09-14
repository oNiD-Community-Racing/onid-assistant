package io.github.nocomment1105.onidassistant.extensions

import com.kotlindiscord.kord.extensions.commands.application.slash.publicSubCommand
import com.kotlindiscord.kord.extensions.extensions.Extension
import com.kotlindiscord.kord.extensions.extensions.publicSlashCommand
import com.kotlindiscord.kord.extensions.types.respond

class PasswordCommands : Extension() {
	override val name: String = "password-commands"

	override suspend fun setup() {
		publicSlashCommand {
			name = "password"
			description = "The parent command for password commands"

			publicSubCommand {
				name = "sgp"
				description = "For when someone asks what the SGP password is"

				action {
					respond {
						content =
							"https://discord.com/channels/800485491580862484/802294502110658620/1025503680230858843"
					}
				}
			}

			publicSubCommand {
				name = "server"
				description = "For when someone asks what the server password is"

				action {
					respond {
						content = "It's on SGP!"
					}
				}
			}
		}
	}
}
