package com.szastarek.text.rpg.shared.plugin

import com.szastarek.text.rpg.shared.config.ApplicationProperties
import com.szastarek.text.rpg.shared.config.ConfigKey
import com.szastarek.text.rpg.shared.config.getStringProperty
import io.ktor.server.application.Application
import kotlinx.datetime.Clock
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import org.koin.core.context.GlobalContext.loadKoinModules
import org.koin.dsl.bind
import org.koin.dsl.module
import org.litote.kmongo.id.serialization.IdKotlinXSerializationModule

@OptIn(ExperimentalSerializationApi::class)
internal val sharedKoinModule =
	module {
		single {
			Json {
				encodeDefaults = true
				prettyPrint = true
				ignoreUnknownKeys = true
				explicitNulls = false
				serializersModule = IdKotlinXSerializationModule
			}
		}
		single {
			ApplicationProperties(
				environment = getStringProperty(ConfigKey("application.env")),
				webClientAppUrl = getStringProperty(ConfigKey("application.webClientAppUrl")),
			)
		}
		single { Clock.System } bind Clock::class
	}

internal fun Application.configureKoin() {
	loadKoinModules(sharedKoinModule)
}
