package com.szastarek.text.rpg.world.adapter.rest

import io.ktor.server.application.Application
import io.ktor.server.application.call
import io.ktor.server.response.respondText
import io.ktor.server.routing.get
import io.ktor.server.routing.routing

fun Application.configureWorldRouting() {
	routing {
		get(WorldApi.V1) {
			call.respondText("Hello, world!")
		}
	}
}
