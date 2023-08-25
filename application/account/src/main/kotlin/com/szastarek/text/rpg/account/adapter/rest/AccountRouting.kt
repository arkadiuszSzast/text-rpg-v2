package com.szastarek.text.rpg.account.adapter.rest

import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktor.server.application.Application
import io.ktor.server.application.call
import io.ktor.server.response.respondText
import io.ktor.server.routing.get
import io.ktor.server.routing.routing

fun Application.configureAccountRouting() {

    routing {
        get(AccountApi.v1) {
            call.respondText("Hello, world!")
        }
    }
}
