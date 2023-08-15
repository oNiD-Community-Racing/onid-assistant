package io.github.nocomment1105.onidassistant.extensions

import com.kotlindiscord.kord.extensions.extensions.Extension
import com.kotlindiscord.kord.extensions.utils.scheduling.Scheduler
import com.kotlindiscord.kord.extensions.utils.scheduling.Task
import dev.kord.core.behavior.channel.createMessage
import dev.kord.core.behavior.getChannelOf
import dev.kord.core.entity.channel.TextChannel
import io.github.nocomment1105.onidassistant.api.Youtube
import io.github.nocomment1105.onidassistant.utils.GUILD
import io.github.nocomment1105.onidassistant.utils.STREAM_CHANNEL
import io.github.nocomment1105.onidassistant.utils.YOUTUBE_CHANNEL_ID
import kotlin.time.Duration.Companion.minutes

private const val YOUTUBE_WATCH_LINK = "https://www.youtube.com/watch?v=<ID>"

class YouTubeLiveNotification : Extension() {
	override val name: String = "youtube-live-notification"

	private val notificationScheduler = Scheduler()

	private lateinit var notificationTask: Task

	private var currentStreamId: String? = null

	override suspend fun setup() {
		notificationTask =
			notificationScheduler.schedule(15.minutes.inWholeSeconds, repeat = true, callback = ::searchForStreams)
	}

	private suspend fun searchForStreams() {
		val streams = Youtube().getCurrentLiveStreamsForChannel(YOUTUBE_CHANNEL_ID)
		val stream = if (streams.items?.isNotEmpty() == true) {
			streams.items[0]
		} else {
			null
		}

		currentStreamId = if (stream != null) { // Yey we found a stream...
			if (currentStreamId == null) { // We haven't got a stream saved yet...
				stream.id.videoId //  ... Lets save it.
			} else { // ... Oh, we've already found this stream...
				return // ... and already posted about the stream, so let's just end this here.
			}
		} else { // Boo, no stream found...
			if (currentStreamId != null) { // ... Oh, we still have one saved ...
				null // ... No streams exist, so lets clear this
			} else { // ... Well we have nothing saved ...
				return // ... Let's just end this here
			}
		}

		if (currentStreamId != null) {
			val guild = kord.getGuild(GUILD)
			val channel = guild.getChannelOf<TextChannel>(STREAM_CHANNEL)

			channel.createMessage {
				content =
					"**${stream?.snippet?.title}**\n\nHey @everyone, ${stream?.snippet?.channelTitle} just went live at ${
						YOUTUBE_WATCH_LINK.replace("<ID>", currentStreamId!!)
					}"
			}
		}
	}
}
