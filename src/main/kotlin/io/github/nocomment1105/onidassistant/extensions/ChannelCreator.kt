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
import com.kotlindiscord.kord.extensions.modules.unsafe.types.InitialSlashCommandResponse
import com.kotlindiscord.kord.extensions.modules.unsafe.types.ackEphemeral
import com.kotlindiscord.kord.extensions.modules.unsafe.types.respondEphemeral
import dev.kord.common.entity.Overwrite
import dev.kord.common.entity.OverwriteType
import dev.kord.common.entity.Permission
import dev.kord.common.entity.Permissions
import dev.kord.common.entity.Snowflake
import dev.kord.core.behavior.channel.createNewsChannel
import dev.kord.core.behavior.channel.createStageChannel
import dev.kord.core.behavior.channel.createTextChannel
import dev.kord.core.behavior.channel.createVoiceChannel
import dev.kord.core.behavior.channel.editRolePermission
import dev.kord.core.behavior.createCategory
import dev.kord.core.behavior.interaction.modal
import dev.kord.core.entity.Role
import dev.kord.core.entity.channel.CategorizableChannel
import dev.kord.core.entity.channel.Category
import io.github.nocomment1105.onidassistant.utils.EventGame

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
                if (arguments.game == EventGame.RACEROOM) {
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

				when (arguments.game) {
					EventGame.AC -> channels(
							EventGame.AC,
							modalObj.customChannelName.value ?: modalObj.eventName.value!!,
							category,
							arguments.eventRole,
							arguments.attendance,
							arguments.splitVCDB
						)
					EventGame.ACC -> channels(
							EventGame.ACC,
							modalObj.customChannelName.value ?: modalObj.eventName.value!!,
							category,
							arguments.eventRole,
							arguments.attendance,
							arguments.splitVCDB
						)
					EventGame.AMS2 -> channels(
							EventGame.AMS2,
							modalObj.customChannelName.value ?: modalObj.eventName.value!!,
							category,
							arguments.eventRole,
							attendance = false,
							splitVCDB = false
						)

					EventGame.RACEROOM -> { /* Can't get here */ }
				}

                respondEphemeral { content = "Created channels!" }
            }
        }
    }

    inner class ChannelsArgs : Arguments() {
        /** The game the event is being run on. */
        val game by enumChoice<EventGame> {
            typeName = "event-game"
            name = "event-game"
            description = "The game being used for this event."
            choices = mutableMapOf(
                "ACC" to EventGame.ACC,
                "AC" to EventGame.AC,
                "Raceroom" to EventGame.RACEROOM,
                "AMS2" to EventGame.AMS2
            )
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

        /** Whether to split the DB and VC channels into a Stage and Voice channel or not. */
        val splitVCDB by boolean {
            name = "split-vc-db"
            description = "Whether to split the Drivers Briefing and VC channels"
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
     * @param game The game the event is being run on
     * @param name The name of the event to put into the channel names
     * @param category The [Category] to create the channels in
     * @param eventRole The [Role] users will require to see these channels.
     *  Used to deny [Send Messages][Permission.SendMessages] in certain channels.
     * @param attendance Whether to create a channel for logging attendance or not
     * @param splitVCDB Whether to split the Driver Briefing channel and VC into a separate Stage and Voice channel
     */
    private suspend fun channels(
        game: EventGame,
        name: String,
        category: Category,
        eventRole: Role,
        attendance: Boolean,
        splitVCDB: Boolean
    ) {
        category.createNewsChannel("$name-announcements").denySendMessages(eventRole.id)
        category.createTextChannel("$name-chat")
		if (game != EventGame.AMS2) {
			category.createTextChannel("$name-outcomes").denySendMessages(eventRole.id)
		}
        if (game == EventGame.ACC) {
            category.createTextChannel("$name-liveries").denySendMessages(eventRole.id)
            category.createTextChannel("$name-practice-server").denySendMessages(eventRole.id)
        }
        if (attendance) category.createTextChannel("$name-attendance").denySendMessages(eventRole.id)
        if (splitVCDB) {
            category.createStageChannel("$name Driver Briefing")
            category.createVoiceChannel("$name VC")
        } else {
            category.createVoiceChannel("$name VC and DB")
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
