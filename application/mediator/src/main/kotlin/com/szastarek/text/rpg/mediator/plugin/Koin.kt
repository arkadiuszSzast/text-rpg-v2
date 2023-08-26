package com.szastarek.text.rpg.mediator.plugin

import io.ktor.server.application.Application
import org.koin.core.context.loadKoinModules
import org.koin.dsl.module

internal val mediatorKoinModule = module {
    single {  }
}

internal fun Application.configureKoin() {
    loadKoinModules(mediatorKoinModule)
}
