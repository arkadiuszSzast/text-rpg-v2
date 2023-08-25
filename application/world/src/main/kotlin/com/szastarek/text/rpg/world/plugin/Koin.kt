package com.szastarek.text.rpg.world.plugin

import io.ktor.server.application.Application
import io.ktor.server.application.install
import org.koin.core.context.GlobalContext
import org.koin.core.context.loadKoinModules
import org.koin.dsl.module
import org.koin.ktor.plugin.Koin

internal val worldModule = module {
}

internal fun Application.configureKoin() {
    if (GlobalContext.getOrNull() == null) {
        install(Koin)
    }
    loadKoinModules(worldModule)
}
