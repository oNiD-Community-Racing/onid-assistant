package io.github.nocomment1105.onidassistant

import com.kotlindiscord.kord.extensions.ExtensibleBot
import io.github.nocomment1105.onidassistant.extensions.ChannelCreator
import io.github.nocomment1105.onidassistant.extensions.PasswordCommands
import io.github.nocomment1105.onidassistant.utils.TOKEN

suspend fun main() {
	val bot = ExtensibleBot(TOKEN) {
		extensions {
			add(::ChannelCreator)
			add(::PasswordCommands)
		}
	}
	bot.start()
}
