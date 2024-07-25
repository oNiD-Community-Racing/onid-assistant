package io.github.nocomment1105.onidassistant.extensions

import com.kotlindiscord.kord.extensions.checks.anyGuild
import com.kotlindiscord.kord.extensions.checks.hasPermission
import com.kotlindiscord.kord.extensions.commands.Arguments
import com.kotlindiscord.kord.extensions.commands.application.slash.converters.impl.enumChoice
import com.kotlindiscord.kord.extensions.commands.converters.impl.boolean
import com.kotlindiscord.kord.extensions.commands.converters.impl.channel
import com.kotlindiscord.kord.extensions.commands.converters.impl.optionalRole
import com.kotlindiscord.kord.extensions.commands.converters.impl.role
import com.kotlindiscord.kord.extensions.components.forms.ModalForm
import com.kotlindiscord.kord.extensions.extensions.Extension
import com.kotlindiscord.kord.extensions.extensions.ephemeralSlashCommand
import com.kotlindiscord.kord.extensions.modules.unsafe.annotations.UnsafeAPI
import com.kotlindiscord.kord.extensions.modules.unsafe.extensions.unsafeSlashCommand
import com.kotlindiscord.kord.extensions.modules.unsafe.types.InitialSlashCommandResponse
import com.kotlindiscord.kord.extensions.modules.unsafe.types.ackEphemeral
import com.kotlindiscord.kord.extensions.modules.unsafe.types.respondEphemeral
import dev.kord.common.entity.ChannelType
import dev.kord.common.entity.Overwrite
import dev.kord.common.entity.OverwriteType
import dev.kord.common.entity.Permission
import dev.kord.common.entity.Permissions
import dev.kord.common.entity.Snowflake
import dev.kord.core.behavior.channel.createNewsChannel
import dev.kord.core.behavior.channel.createStageChannel
import dev.kord.core.behavior.channel.createTextChannel
import dev.kord.core.behavior.channel.createVoiceChannel
import dev.kord.core.behavior.channel.edit
import dev.kord.core.behavior.channel.editRolePermission
import dev.kord.core.behavior.getChannelOf
import dev.kord.core.behavior.interaction.modal
import dev.kord.core.entity.Role
import dev.kord.core.entity.channel.CategorizableChannel
import dev.kord.core.entity.channel.Category
import io.github.nocomment1105.onidassistant.database.collections.ChannelCreatorConfigCollection
import io.github.nocomment1105.onidassistant.utils.CategoryType

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
				val categoryId = ChannelCreatorConfigCollection().getCategoryId(guild!!.id, arguments.category)
				val category = categoryId?.let { channelId -> guild!!.getChannelOf<Category>(channelId) }
				if (category == null) {
					ackEphemeral()
					respondEphemeral {
						content = "The category you have configured is invalid. Please re-set it and try again"
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

				val permissions = mutableListOf(
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
				// Add the extra role if it exists, if not just go about business as usual.
				if (arguments.extraRole != null) {
					permissions.add(
						Overwrite(
							arguments.extraRole!!.id,
							OverwriteType.Role,
							Permissions(Permission.ViewChannel),
							Permissions()
						)
					)
				}

				channels(
					modalObj.channelPrefix.value!!,
					category,
					arguments.eventRole,
					arguments.attendance,
					arguments.briefing,
					permissions
				)

				respondEphemeral { content = "Created channels!" }
			}
		}

		ephemeralSlashCommand(::ConfigArgs) {
			name = "category-config"
			description = "Configure the categories for channels to get added too"

			check {
				anyGuild()
				requirePermission(Permission.ManageChannels)
			}

			action {
				ChannelCreatorConfigCollection().setConfig(
					guild!!.id,
					arguments.acCategory.id,
					arguments.accCategory.id,
					arguments.ams2Category.id,
					arguments.enduroCategory.id,
					arguments.consoleCategory.id,
					arguments.otherCategory.id
				)
				respond {
					content = "Config set"
				}
			}
		}
	}

	inner class ChannelsArgs : Arguments() {
		val category by enumChoice<CategoryType> {
			name = "category"
			description = "The category to put the channels under"
			typeName = "Category For Channels"
		}

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

		/** Whether to create a briefing stage channel or not. */
		val briefing by boolean {
			name = "briefing-channel"
			description = "Whether to create a briefing channel or not"
		}

		/** An optional extra role that may need access to the channels in the category. */
		val extraRole by optionalRole {
			name = "extra-role"
			description = "An optional extra role that may need to see these channels."
		}
	}

	inner class ConfigArgs : Arguments() {
		val acCategory by channel {
			name = "ac-category"
			description = "The Assetto Corsa Category"
			requireChannelType(ChannelType.GuildCategory)
		}
		val accCategory by channel {
			name = "acc-category"
			description = "The Assetto Corsa Competizione Category"
			requireChannelType(ChannelType.GuildCategory)
		}
		val ams2Category by channel {
			name = "ams2-category"
			description = "The Automobilista 2 Category"
			requireChannelType(ChannelType.GuildCategory)
		}
		val enduroCategory by channel {
			name = "enduro-category"
			description = "The Enduro Category"
			requireChannelType(ChannelType.GuildCategory)
		}
		val consoleCategory by channel {
			name = "console-category"
			description = "The Console Category"
			requireChannelType(ChannelType.GuildCategory)
		}
		val otherCategory by channel {
			name = "other-category"
			description = "The Other Category"
			requireChannelType(ChannelType.GuildCategory)
		}
	}

	inner class NameModal : ModalForm() {
		override var title: String = "Event name configuration"

		/** The prefix for the channels for the event. */
		val channelPrefix = lineText {
			label = "Event name prefix"
			placeholder = "The shortened name to use for the channels. E.G.: ams"
			required = true
		}
	}

	/**
	 * Creates all the channels within the specified [category] for an oNiD Event.
	 *
	 * @param name The name of the event to put into the channel names
	 * @param category The [Category] to create the channels in
	 * @param eventRole The [Role] users will require to see these channels.
	 *  Used to deny [Send Messages][Permission.SendMessages] in certain channels.
	 * @param attendance Whether to create a channel for logging attendance or not.
	 * @param briefing Whether to create a Stage Channel for Driver briefings or not.
	 */
	private suspend fun channels(
		name: String,
		category: Category,
		eventRole: Role,
		attendance: Boolean,
		briefing: Boolean,
		permissions: List<Overwrite>
	) {
		category.createNewsChannel("$name-announcements").edit {
			permissions.forEach { addOverwrite(it) }
		}.denySendMessages(eventRole.id)
		category.createTextChannel("$name-chat").edit {
			permissions.forEach { addOverwrite(it) }
		}
		if (attendance) {
			category.createTextChannel("$name-attendance").edit {
				permissions.forEach { addOverwrite(it) }
			}.denySendMessages(eventRole.id)
		}
		if (briefing) {
			category.createStageChannel("$name-briefing").edit {
				permissions.forEach { addOverwrite(it) }
			}
		}
		category.createVoiceChannel("$name VC").edit {
			permissions.forEach { addOverwrite(it) }
		}
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
