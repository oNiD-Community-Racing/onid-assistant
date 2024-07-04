package io.github.nocomment1105.onidassistant.utils

import com.kotlindiscord.kord.extensions.commands.application.slash.converters.ChoiceEnum

enum class CategoryType(override val readableName: String) : ChoiceEnum {
	AC("Assetto Corsa"),
	ACC("Assetto Corsa Competizione"),
	AMS2("Automobilista 2"),
	ENDURO("Enduro"),
	CONSOLE("Console"),
	OTHER("Other")
}
