package io.github.nocomment1105.onidassistant.utils

import dev.kordex.core.commands.application.slash.converters.ChoiceEnum
import dev.kordex.core.i18n.toKey
import dev.kordex.core.i18n.types.Key

enum class CategoryType(override val readableName: Key) : ChoiceEnum {
	AC("Assetto Corsa".toKey()),
	ACC("Assetto Corsa Competizione".toKey()),
	AMS2("Automobilista 2".toKey()),
	ENDURO("Enduro".toKey()),
	CONSOLE("Console".toKey()),
	OTHER("Other".toKey())
}
