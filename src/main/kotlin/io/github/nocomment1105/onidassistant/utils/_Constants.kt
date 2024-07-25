package io.github.nocomment1105.onidassistant.utils

import com.kotlindiscord.kord.extensions.utils.env
import com.kotlindiscord.kord.extensions.utils.envOrNull
import dev.kord.common.entity.Snowflake

val TOKEN = env("TOKEN")

val MONGO_URI = envOrNull("MONGO_URI") ?: "mongodb://localhost:27017"

val SG_API_KEY = envOrNull("SG_API_KEY") ?: ""

val GUILD_ID = Snowflake(env("GUILD_ID"))
