package com.szastarek.text.rpg.event.store

import com.szastarek.text.rpg.event.store.plugin.configureKoin
import io.ktor.server.application.Application
import org.koin.ktor.ext.get

fun Application.eventStoreModule() {
	configureKoin()
}
