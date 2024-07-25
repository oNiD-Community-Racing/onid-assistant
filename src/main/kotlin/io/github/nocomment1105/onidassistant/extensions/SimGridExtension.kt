package io.github.nocomment1105.onidassistant.extensions

import com.kotlindiscord.kord.extensions.commands.Arguments
import com.kotlindiscord.kord.extensions.commands.converters.impl.int
import com.kotlindiscord.kord.extensions.extensions.Extension
import com.kotlindiscord.kord.extensions.extensions.publicSlashCommand
import com.kotlindiscord.kord.extensions.pagination.PublicResponsePaginator
import com.kotlindiscord.kord.extensions.pagination.pages.Page
import com.kotlindiscord.kord.extensions.pagination.pages.Pages
import com.kotlindiscord.kord.extensions.time.TimestampType
import com.kotlindiscord.kord.extensions.time.toDiscord
import com.kotlindiscord.kord.extensions.utils.scheduling.Scheduler
import com.kotlindiscord.kord.extensions.utils.scheduling.Task
import dev.kord.common.entity.GuildScheduledEventEntityMetadata
import dev.kord.common.entity.GuildScheduledEventPrivacyLevel
import dev.kord.common.entity.ScheduledEntityType
import dev.kord.common.entity.optional.optional
import dev.kord.core.behavior.createScheduledEvent
import dev.kord.rest.builder.message.embed
import io.github.nocomment1105.onidassistant.api.SimGrid
import io.github.nocomment1105.onidassistant.utils.GUILD_ID
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.toList
import kotlinx.datetime.Clock
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.hours

class SimGridExtension : Extension() {
	override val name: String = "simgrid-commands"

	private val scheduler = Scheduler()

	private lateinit var task: Task

	override suspend fun setup() {
		task = scheduler.schedule(12.hours.inWholeSeconds, repeat = true, callback = ::checkAndCreateEvents)

		publicSlashCommand {
			name = "view-all-events"
			description = "View all the events oNiD has on SimGrid"

			action {
				val pagesObj = Pages()

				val events = SimGrid().getChampionships()

				if (events.isEmpty()) {
					pagesObj.addPage(
						Page {
							description = "There are currently no events."
						}
					)
				} else {
					events.chunked(10).forEach { chunk ->
						var string = ""
						chunk.forEach {
							string += "Event name: ${it.name}\nEvent id: ${it.id}\n---\n"
						}

						pagesObj.addPage(
							Page {
								title = "Events for oNiD Racing"
								description = string
								footer {
									text = "Use the ID of the event in /view-event-details to get more information."
								}
							}
						)
					}
				}

				val paginator = PublicResponsePaginator(
					pages = pagesObj,
					owner = event.interaction.user,
					timeoutSeconds = 500,
					interaction = interactionResponse
				)

				paginator.send()
			}
		}

		publicSlashCommand(::DetailsArgs) {
			name = "view-event-details"
			description = "View more in-depth details about an oNiD event"

			action {
				val now = Clock.System.now()
				val sgEvent = SimGrid().getChampionshipDetails(arguments.eventId)

				respond {
					embed {
						author {
							name = sgEvent.name
							url = sgEvent.url
						}
						description = "This event has ${sgEvent.races.size} races"
						field {
							name = "Series Start date"
							value = sgEvent.startDate.toDiscord(TimestampType.ShortDateTime)
						}
						field {
							name = "Registrations open?"
							value = when (sgEvent.acceptingRegistrations) {
								true -> "Yes"
								false -> "No"
							}
						}
						field {
							name = "Capacity"
							value = sgEvent.capacity.toString()
						}
						field {
							name = "Team event?"
							value = when (sgEvent.teamsEnabled) {
								true -> "Yes"
								false -> "No"
							}
						}
						if (sgEvent.entryFeeRequired == true && sgEvent.entryFeeCents != null) {
							field {
								name = "Entry fee"
								value = "$${sgEvent.entryFeeCents / 100}"
							}
						}
						footer {
							text = "https://www.twitch.tv/onidracing"
						}
					}
					embed {
						title = "Events"
						sgEvent.races.sortedBy { it.startsAt }.forEach {
							val completed = now.epochSeconds - it.startsAt.epochSeconds > 0
							field {
								name = it.raceName
								value =
									"Track: ${it.track}\nStart time: " +
										"${it.startsAt.toDiscord(TimestampType.ShortDateTime)}\nCompleted: $completed"
								inline = true
							}
						}
						image = sgEvent.image
						footer {
							text = sgEvent.gameName
						}
						timestamp = now
					}
				}
			}
		}

		publicSlashCommand {
			name = "simgrid-stats"
			description = "View your simgrid stats!"

			action {
				val user = SimGrid().getUser(event.interaction.user.id)
				// Map each game ranking to it's ID so they remain connected
				val gameIds = mutableMapOf<Int, Int>()
				user.gridRatings.forEach {
					gameIds[it.gameId] = it.rating
				}

				respond {
					embed {
						author {
							name = user.preferredName
							url = "https://thesimgrid.com/drivers/${user.userId}-${user.username}/"
							icon = event.interaction.user.avatar?.cdnUrl?.toUrl()
						}
						field {
							name = "Total races started"
							value = if (user.totalRacesStarted == 0) "0 :(" else user.totalRacesStarted.toString()
						}
						field {
							name = "Total wins (Win %)"
							value = "${user.totalWins} (${(user.totalWins.toDouble() / user.totalRacesStarted.toDouble()) * 100}%)"
						}
						field {
							name = "Total podiums (Podium %)"
							value = "${user.totalPodiums} (${(user.totalPodiums.toDouble() / user.totalRacesStarted.toDouble()) * 100}%)"
						}
					}
					embed {
						title = "Game rankings"
						if (gameIds.isNotEmpty()) {
							gameIds.forEach {
								// If the ID list is not empty we can pretty much guarantee this function will not be null
								val game = SimGrid().getGameById(it.key)!!
								field {
									name = game.name
									value = it.value.toString()
								}
							}
						}
					}
				}
			}
		}
	}

	private suspend inline fun checkAndCreateEvents() {
		val now = Clock.System.now()
		val weekLater = now.plus(7.days)
		val guild = kord.getGuild(GUILD_ID)
		val championshipsDetailed = SimGrid().getDetailedChampionships()
		val existingEvents = mutableListOf<String>()
		// Collect the names of existing events into a list
		guild.scheduledEvents.toList().forEach {
			existingEvents.add(it.name)
		}

		// Loop through each championship
		for (championship in championshipsDetailed) {
			// Loop through each race in the championship
			for (race in championship.races) {
				// Check if the race name is an existing event, and move on to the next race if true
				if (existingEvents.contains(race.raceName)) continue
				// Check if the start time is less than a week away and if the race is in the past or not
				if ((race.startsAt < weekLater) && ((race.startsAt.epochSeconds - now.epochSeconds) > 0)) {
					// Create the event with the same name as the race name, as a guild only event, with a custom
					// location. Start time is event start time
					guild.createScheduledEvent(
						race.raceName, GuildScheduledEventPrivacyLevel.GuildOnly, race.startsAt,
						ScheduledEntityType.External
					) {
						// Set the championship name as the description
						description = championship.name
						// Set the location as the track nane
						entityMetadata = GuildScheduledEventEntityMetadata(race.track.optional())
					}
				}
			}
			// Just to try and avoid pounding rate limits
			delay(250)
		}
	}

	inner class DetailsArgs : Arguments() {
		val eventId by int {
			name = "event-id"
			description = "The ID for the event you are looking up"
		}
	}
}
