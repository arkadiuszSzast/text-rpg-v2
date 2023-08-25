package com.szastarek.text.rpg.documentation

import com.szastarek.text.rpg.documentation.plugin.configureDocumentation
import com.szastarek.text.rpg.documentation.plugin.configureKoin
import io.ktor.server.application.Application
import org.koin.ktor.ext.get

fun Application.documentationModule() {
    configureKoin()
    configureDocumentation(get())
}
