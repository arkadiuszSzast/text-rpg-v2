package com.szastarek.text.rpg.mediator

import com.szastarek.text.rpg.mediator.plugin.configureKoin
import io.ktor.server.application.Application

fun Application.mediatorModule() {
    configureKoin()
}
