package com.szastarek.text.rpg.shared

import com.szastarek.text.rpg.shared.plugin.configureKoin
import com.szastarek.text.rpg.shared.plugin.configureSerialization
import io.ktor.server.application.Application
import org.koin.ktor.ext.get

fun Application.sharedModule() {
	configureKoin()
	configureSerialization(get())
}
