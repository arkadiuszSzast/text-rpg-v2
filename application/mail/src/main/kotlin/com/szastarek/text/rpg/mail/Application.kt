package com.szastarek.text.rpg.mail

import com.szastarek.text.rpg.mail.plugin.configureKoin
import io.ktor.server.application.Application

fun Application.mailModule() {
    configureKoin()
}
