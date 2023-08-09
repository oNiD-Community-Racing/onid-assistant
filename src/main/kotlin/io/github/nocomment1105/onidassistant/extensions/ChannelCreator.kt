package io.github.nocomment1105.onidassistant.extensions

import com.kotlindiscord.kord.extensions.checks.anyGuild
import com.kotlindiscord.kord.extensions.checks.hasPermission
import com.kotlindiscord.kord.extensions.commands.Arguments
import com.kotlindiscord.kord.extensions.commands.application.slash.converters.impl.enumChoice
import com.kotlindiscord.kord.extensions.commands.converters.impl.boolean
import com.kotlindiscord.kord.extensions.commands.converters.impl.role
import com.kotlindiscord.kord.extensions.components.forms.ModalForm
import com.kotlindiscord.kord.extensions.extensions.Extension
import com.kotlindiscord.kord.extensions.modules.unsafe.annotations.UnsafeAPI
import com.kotlindiscord.kord.extensions.modules.unsafe.extensions.unsafeSlashCommand
import com.kotlindiscord.kord.extensions.modules.unsafe.types.ackEphemeral
import com.kotlindiscord.kord.extensions.modules.unsafe.types.respondEphemeral
import dev.kord.common.entity.Overwrite
import dev.kord.common.entity.OverwriteType
import dev.kord.common.entity.Permission
import dev.kord.common.entity.Permissions
import dev.kord.core.behavior.GuildBehavior
import dev.kord.core.behavior.channel.createStageChannel
import dev.kord.core.behavior.channel.createTextChannel
import dev.kord.core.behavior.channel.createVoiceChannel
import dev.kord.core.behavior.createCategory
import dev.kord.core.behavior.interaction.modal
import dev.kord.core.entity.Role
import dev.kord.core.entity.channel.Category
import io.github.nocomment1105.onidassistant.utils.EventGame

// TODO Actually test this
// TODO Write some documentation
class ChannelCreator : Extension() {
	override val name: String = "channel-creator"

	@OptIn(UnsafeAPI::class)
	override suspend fun setup() {
		unsafeSlashCommand(::ChannelsArgs) {
			name = "onid-channels"
			description = "Create the channels required for an oNiD event/series"

			check {
				anyGuild()
				hasPermission(Permission.ManageChannels)
			}

			action {
				if (arguments.game == EventGame.AMS2 || arguments.game == EventGame.RACEROOM) {
					ackEphemeral()
					respondEphemeral {
						content = "This game type is not supported yet. Contact `nocomment1105` for more information!"
					}
					return@action
				}

				val modalObj = NameModal()

				this@unsafeSlashCommand.componentRegistry.register(modalObj)

				event.interaction.modal(modalObj.title, modalObj.id) {
					modalObj.applyToBuilder(this, getLocale(), null)
				}

				modalObj.awaitCompletion { modalSubmitInteraction ->
					interactionResponse = modalSubmitInteraction?.deferEphemeralMessageUpdate()
				}

				if (arguments.game == EventGame.AC) {
					acChannels(
						guild!!,
						modalObj.eventName.value!!,
						modalObj.customChannelName.value,
						arguments.eventRole,
						arguments.attendance,
						arguments.splitVCDB
					)
				} else if (arguments.game == EventGame.ACC) {
					accChannels(
						guild!!,
						modalObj.eventName.value!!,
						modalObj.customChannelName.value,
						arguments.eventRole,
						arguments.attendance,
						arguments.splitVCDB
					)
				}

				ackEphemeral()
				respondEphemeral { content = "Created channels!" }
			}
		}
	}

	inner class ChannelsArgs : Arguments() {
		val game by enumChoice<EventGame> {
			name = "event-game"
			description = "The game being used for this event."
			choices = mutableMapOf(
				"ACC" to EventGame.ACC,
				"AC" to EventGame.AC,
				"Raceroom" to EventGame.RACEROOM,
				"AMS2" to EventGame.AMS2
			)
		}
		val eventRole by role {
			name = "event-role"
			description = "The role users must have to see the channels"
		}
		val attendance by boolean {
			name = "attendance-channel"
			description = "Whether to create an attendance logging channel or not"
		}
		val splitVCDB by boolean {
			name = "split-vc-db"
			description = "Whether to split the Drivers Briefing and VC channels"
		}
	}

	inner class NameModal : ModalForm() {
		override var title: String = "Event name configuration"

		val eventName = lineText {
			label = "What is the name of the event"
			placeholder = "Porsche Cup Series"
			required = true
		}

		val customChannelName = lineText {
			label = "Channel name prefixes. Leave blank to use the event name."
			placeholder = "mws"
			required = false
		}
	}

	private suspend fun acChannels(
		guild: GuildBehavior,
		name: String,
		channelPrefix: String?,
		eventRole: Role,
		attendance: Boolean,
		splitVCDB: Boolean
	) {
		val category = guild.createCategory(name) {
			permissionOverwrites.add(
				Overwrite(guild.id, OverwriteType.Role, Permissions(), Permissions(Permission.ViewChannel))
			)
			permissionOverwrites.add(
				Overwrite(eventRole.id, OverwriteType.Role, Permissions(Permission.ViewChannel), Permissions())
			)
		}
		if (channelPrefix == null) {
			channels(EventGame.AC, name, category, attendance, splitVCDB)
		} else {
			channels(EventGame.AC, channelPrefix, category, attendance, splitVCDB)
		}
	}

	private suspend fun accChannels(
		guild: GuildBehavior,
		name: String,
		channelPrefix: String?,
		eventRole: Role,
		attendance: Boolean,
		splitVCDB: Boolean
	) {
		val category = guild.createCategory(name) {
			permissionOverwrites.add(
				Overwrite(guild.id, OverwriteType.Role, Permissions(), Permissions(Permission.ViewChannel))
			)
			permissionOverwrites.add(
				Overwrite(eventRole.id, OverwriteType.Role, Permissions(Permission.ViewChannel), Permissions())
			)
		}
		if (channelPrefix == null) {
			channels(EventGame.ACC, name, category, attendance, splitVCDB)
		} else {
			channels(EventGame.ACC, name, category, attendance, splitVCDB)
		}
	}

	private suspend fun channels(
		game: EventGame,
		name: String,
		category: Category,
		attendance: Boolean,
		splitVCDB: Boolean
	) {
		category.createTextChannel("$name-chat")
		category.createTextChannel("$name-outcomes")
		if (game == EventGame.ACC) {
			category.createTextChannel("$name-liveries")
			category.createTextChannel("$name-practice-server")
		}
		if (attendance) category.createTextChannel("$name-attendance")
		if (splitVCDB) {
			category.createStageChannel("$name Drive Briefing")
			category.createVoiceChannel("$name VC")
		} else {
			category.createVoiceChannel("$name VC and DB")
		}
	}
}
