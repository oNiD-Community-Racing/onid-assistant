package io.github.nocomment1105.onidassistant.utils

import com.kotlindiscord.kord.extensions.utils.env
import dev.kord.common.entity.Snowflake

val TOKEN = env("TOKEN")
val GOOGLE_API_KEY = env("GOOGLE_API_KEY")
val YOUTUBE_CHANNEL_ID = env("YOUTUBE_CHANNEL_ID")
val GUILD = Snowflake(env("GUILD"))
val STREAM_CHANNEL = Snowflake(env("STREAM_CHANNEL"))
