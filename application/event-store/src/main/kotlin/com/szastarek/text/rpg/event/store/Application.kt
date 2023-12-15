package com.szastarek.text.rpg.event.store

import com.szastarek.text.rpg.event.store.plugin.configureKoin
import io.ktor.server.application.Application

fun Application.eventStoreModule() {
	configureKoin()
}
