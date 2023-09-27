package com.szastarek.text.rpg

import com.szastarek.text.rpg.account.accountModule
import com.szastarek.text.rpg.world.worldModule
import io.ktor.server.application.Application
import org.koin.ktor.ext.getKoin

fun main(args: Array<String>) {
    io.ktor.server.netty.EngineMain.main(args)
}

fun Application.main() {
    accountModule()
    worldModule()
    getKoin().createEagerInstances()
}
