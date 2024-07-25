package io.github.nocomment1105.onidassistant

import com.kotlindiscord.kord.extensions.ExtensibleBot
import io.github.nocomment1105.onidassistant.extensions.ChannelCreator
import io.github.nocomment1105.onidassistant.extensions.PasswordCommands
import io.github.nocomment1105.onidassistant.extensions.SimGridExtension
import io.github.nocomment1105.onidassistant.utils.TOKEN
import io.github.nocomment1105.onidassistant.utils.database

suspend fun main() {
	val bot = ExtensibleBot(TOKEN) {
		database(true)
		extensions {
			add(::ChannelCreator)
			add(::PasswordCommands)
			add(::SimGridExtension)
		}
	}
	bot.start()
}
