package io.github.nocomment1105.onidassistant.utils

import com.kotlindiscord.kord.extensions.commands.application.slash.converters.ChoiceEnum

/**
 * The enum for the game the event is for.
 */
enum class EventGame : ChoiceEnum {
	/** The entry of Assetto Corsa Competizione. */
	ACC,

	/** The entry of Assetto Corsa. */
	AC,

	/** The entry of Raceroom. */
	RACEROOM,

	/** The entry of Automobilista 2. */
	AMS2;

	override val readableName: String = "Event Game"
}
