package com.szastarek.text.rpg.shared.plugin

import com.szastarek.text.rpg.shared.config.ApplicationProperties
import com.szastarek.text.rpg.shared.config.ConfigKey
import com.szastarek.text.rpg.shared.config.getStringProperty
import io.ktor.server.application.Application
import kotlinx.serialization.json.Json
import org.koin.core.context.GlobalContext.loadKoinModules
import org.koin.dsl.module
import org.litote.kmongo.id.serialization.IdKotlinXSerializationModule

internal val sharedKoinModule = module {
    single {
        Json {
            encodeDefaults = true
            prettyPrint = true
            ignoreUnknownKeys = true
            serializersModule = IdKotlinXSerializationModule
        }
    }
    single {
        ApplicationProperties(
            environment = getStringProperty(ConfigKey("application.env")),
            webClientAppUrl = getStringProperty(ConfigKey("application.webClientAppUrl"))
        )
    }
}

internal fun Application.configureKoin() {
    loadKoinModules(sharedKoinModule)
}
