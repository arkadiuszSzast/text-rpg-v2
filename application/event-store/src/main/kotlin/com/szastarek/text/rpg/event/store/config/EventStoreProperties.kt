package com.szastarek.text.rpg.event.store.config

data class EventStoreProperties(
	val connectionString: String,
	val reSubscribeOnDrop: Boolean,
)
