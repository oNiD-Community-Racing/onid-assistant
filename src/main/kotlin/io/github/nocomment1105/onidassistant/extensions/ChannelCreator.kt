package io.github.nocomment1105.onidassistant.extensions

import com.kotlindiscord.kord.extensions.checks.anyGuild
import com.kotlindiscord.kord.extensions.checks.hasPermission
import com.kotlindiscord.kord.extensions.commands.Arguments
import com.kotlindiscord.kord.extensions.commands.converters.impl.boolean
import com.kotlindiscord.kord.extensions.commands.converters.impl.role
import com.kotlindiscord.kord.extensions.components.forms.ModalForm
import com.kotlindiscord.kord.extensions.extensions.Extension
import com.kotlindiscord.kord.extensions.modules.unsafe.annotations.UnsafeAPI
import com.kotlindiscord.kord.extensions.modules.unsafe.extensions.unsafeSlashCommand
import com.kotlindiscord.kord.extensions.modules.unsafe.types.InitialSlashCommandResponse
import com.kotlindiscord.kord.extensions.modules.unsafe.types.respondEphemeral
import dev.kord.common.entity.Overwrite
import dev.kord.common.entity.OverwriteType
import dev.kord.common.entity.Permission
import dev.kord.common.entity.Permissions
import dev.kord.common.entity.Snowflake
import dev.kord.core.behavior.channel.createNewsChannel
import dev.kord.core.behavior.channel.createTextChannel
import dev.kord.core.behavior.channel.createVoiceChannel
import dev.kord.core.behavior.channel.editRolePermission
import dev.kord.core.behavior.createCategory
import dev.kord.core.behavior.interaction.modal
import dev.kord.core.entity.Role
import dev.kord.core.entity.channel.CategorizableChannel
import dev.kord.core.entity.channel.Category

class ChannelCreator : Extension() {
	override val name: String = "channel-creator"

	@OptIn(UnsafeAPI::class)
	override suspend fun setup() {
		unsafeSlashCommand(::ChannelsArgs) {
			name = "onid-channels"
			description = "Create the channels required for an oNiD event/series"

			initialResponse = InitialSlashCommandResponse.None

			check {
				anyGuild()
				hasPermission(Permission.ManageChannels)
			}

			action {
				val modalObj = NameModal()

				this@unsafeSlashCommand.componentRegistry.register(modalObj)

				event.interaction.modal(modalObj.title, modalObj.id) {
					modalObj.applyToBuilder(this, getLocale(), null)
				}

				modalObj.awaitCompletion { modalSubmitInteraction ->
					interactionResponse = modalSubmitInteraction?.deferEphemeralMessageUpdate()
				}

				val category = guild!!.createCategory(modalObj.eventName.value!!) {
					permissionOverwrites.addAll(
						listOf(
							Overwrite(
								guild!!.id,
								OverwriteType.Role,
								Permissions(),
								Permissions(Permission.ViewChannel)
							),
							Overwrite(
								arguments.eventRole.id,
								OverwriteType.Role,
								Permissions(Permission.ViewChannel),
								Permissions()
							),
							Overwrite(
								arguments.adminRole.id,
								OverwriteType.Role,
								Permissions(Permission.ViewChannel),
								Permissions()
							)
						)
					)
				}
				channels(
					if (modalObj.customChannelName.value.isNullOrEmpty()) {
						modalObj.eventName.value!!
					} else {
						modalObj.customChannelName.value!!
					},
					category,
					arguments.eventRole,
					arguments.attendance
				)

				respondEphemeral { content = "Created channels!" }
			}
		}
	}

	inner class ChannelsArgs : Arguments() {
		/** The role required to see the channels for the event. */
		val eventRole by role {
			name = "event-role"
			description = "The role users must have to see the channels"
		}

		/** The role for the admins that need to see the channels. */
		val adminRole by role {
			name = "admin-role"
			description = "The role for the admins that need to see the channels"
		}

		/** Whether to create an attendance logging channel or not. */
		val attendance by boolean {
			name = "attendance-channel"
			description = "Whether to create an attendance logging channel or not"
		}
	}

	inner class NameModal : ModalForm() {
		override var title: String = "Event name configuration"

		/** The main name of the event. */
		val eventName = lineText {
			label = "What is the name of the event?"
			placeholder = "Porsche Cup Series"
			required = true
		}

		/** The shortened version of the event name to prefix the channels. */
		val customChannelName = lineText {
			label = "Channel name prefixes."
			placeholder = "Leave blank to use the event name."
			required = false
		}
	}

	/**
	 * Creates all the channels within the specified [category] for an oNiD Event.
	 *
	 * @param name The name of the event to put into the channel names
	 * @param category The [Category] to create the channels in
	 * @param eventRole The [Role] users will require to see these channels.
	 *  Used to deny [Send Messages][Permission.SendMessages] in certain channels.
	 * @param attendance Whether to create a channel for logging attendance or not
	 */
	private suspend fun channels(
		name: String,
		category: Category,
		eventRole: Role,
		attendance: Boolean
	) {
		category.createNewsChannel("$name-announcements").denySendMessages(eventRole.id)
		category.createTextChannel("$name-chat")
		if (attendance) category.createTextChannel("$name-attendance").denySendMessages(eventRole.id)
		category.createVoiceChannel("$name VC")
	}

	/**
	 * Denies the [Send Messages][Permission.SendMessages] permission from the given [roleId].
	 *
	 * @param roleId The ID of the role to deny the permissions to
	 */
	private suspend fun <T : CategorizableChannel> T.denySendMessages(roleId: Snowflake) {
		this.editRolePermission(roleId) { denied = Permissions(Permission.SendMessages) }
	}
}
