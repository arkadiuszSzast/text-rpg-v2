package com.szastarek.text.rpg.event.store

import kotlinx.serialization.Serializable

@JvmInline
@Serializable
value class EventType(val value: String) {
	constructor(eventCategory: EventCategory, type: String) : this("${eventCategory.value}-$type")
}
