package io.github.nocomment1105.onidassistant.extensions

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
import dev.kordex.core.checks.anyGuild
import dev.kordex.core.checks.hasPermission
import dev.kordex.core.commands.Arguments
import dev.kordex.core.commands.application.slash.converters.impl.enumChoice
import dev.kordex.core.commands.converters.impl.boolean
import dev.kordex.core.commands.converters.impl.channel
import dev.kordex.core.commands.converters.impl.optionalRole
import dev.kordex.core.commands.converters.impl.role
import dev.kordex.core.components.forms.ModalForm
import dev.kordex.core.extensions.Extension
import dev.kordex.core.extensions.ephemeralSlashCommand
import dev.kordex.core.i18n.toKey
import dev.kordex.core.i18n.types.Key
import dev.kordex.modules.dev.unsafe.annotations.UnsafeAPI
import dev.kordex.modules.dev.unsafe.commands.slash.InitialSlashCommandResponse
import dev.kordex.modules.dev.unsafe.extensions.unsafeSlashCommand
import io.github.nocomment1105.onidassistant.database.collections.ChannelCreatorConfigCollection
import io.github.nocomment1105.onidassistant.utils.CategoryType

class ChannelCreator : Extension() {
	override val name: String = "channel-creator"

	@OptIn(UnsafeAPI::class)
	override suspend fun setup() {
		unsafeSlashCommand(::ChannelsArgs) {
			name = "onid-channels".toKey()
			description = "Create the channels required for an oNiD event/series".toKey()

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

				event.interaction.modal(modalObj.title.toString(), modalObj.id) {
					modalObj.applyToBuilder(this, getLocale())
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
			name = "category-config".toKey()
			description = "Configure the categories for channels to get added too".toKey()

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
			name = "category".toKey()
			description = "The category to put the channels under".toKey()
			typeName = "Category For Channels".toKey()
		}

		/** The role required to see the channels for the event. */
		val eventRole by role {
			name = "event-role".toKey()
			description = "The role users must have to see the channels".toKey()
		}

		/** The role for the admins that need to see the channels. */
		val adminRole by role {
			name = "admin-role".toKey()
			description = "The role for the admins that need to see the channels".toKey()
		}

		/** Whether to create an attendance logging channel or not. */
		val attendance by boolean {
			name = "attendance-channel".toKey()
			description = "Whether to create an attendance logging channel or not".toKey()
		}

		/** Whether to create a briefing stage channel or not. */
		val briefing by boolean {
			name = "briefing-channel".toKey()
			description = "Whether to create a briefing channel or not".toKey()
		}

		/** An optional extra role that may need access to the channels in the category. */
		val extraRole by optionalRole {
			name = "extra-role".toKey()
			description = "An optional extra role that may need to see these channels.".toKey()
		}
	}

	inner class ConfigArgs : Arguments() {
		val acCategory by channel {
			name = "ac-category".toKey()
			description = "The Assetto Corsa Category".toKey()
			requireChannelType(ChannelType.GuildCategory)
		}
		val accCategory by channel {
			name = "acc-category".toKey()
			description = "The Assetto Corsa Competizione Category".toKey()
			requireChannelType(ChannelType.GuildCategory)
		}
		val ams2Category by channel {
			name = "ams2-category".toKey()
			description = "The Automobilista 2 Category".toKey()
			requireChannelType(ChannelType.GuildCategory)
		}
		val enduroCategory by channel {
			name = "enduro-category".toKey()
			description = "The Enduro Category".toKey()
			requireChannelType(ChannelType.GuildCategory)
		}
		val consoleCategory by channel {
			name = "console-category".toKey()
			description = "The Console Category".toKey()
			requireChannelType(ChannelType.GuildCategory)
		}
		val otherCategory by channel {
			name = "other-category".toKey()
			description = "The Other Category".toKey()
			requireChannelType(ChannelType.GuildCategory)
		}
	}

	inner class NameModal : ModalForm() {
		override var title: Key = "Event name configuration".toKey()

		/** The prefix for the channels for the event. */
		val channelPrefix = lineText {
			label = "Event name prefix".toKey()
			placeholder = "The shortened name to use for the channels. E.G.: ams".toKey()
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
