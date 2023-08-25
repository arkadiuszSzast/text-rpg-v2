package com.szastarek.text.rpg.shared.plugin

import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.Application
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import kotlinx.serialization.json.Json

fun Application.configureSerialization(jsonMapper: Json) {
    installIfNotRegistered(ContentNegotiation) {
        json(jsonMapper)
    }
}
