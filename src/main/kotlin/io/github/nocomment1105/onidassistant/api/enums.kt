package io.github.nocomment1105.onidassistant.api

enum class RacesCount(val value: String) {
	EVENTS("events"),
	FULL_CHAMPIONSHIPS("full_championships"),
}

enum class Status(val value: String) {
	ACTIVE("active"),
	UPCOMING("upcoming"),
	INACTIVE("inactive"),
}

enum class DriverTypes(val value: String) {
	SOLO("solo"),
	TEAMS("teams"),
}

enum class GridType(val value: String) {
	CLOSED("closed"),
	OPENING("opening"),
	OPEN("open"),
	FULL("full")
}
