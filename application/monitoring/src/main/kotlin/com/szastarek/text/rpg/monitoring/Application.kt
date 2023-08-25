package com.szastarek.text.rpg.monitoring

import com.szastarek.text.rpg.monitoring.plugin.configureKoin
import com.szastarek.text.rpg.monitoring.plugin.configureMonitoring
import io.ktor.server.application.Application
import org.koin.ktor.ext.get

fun Application.monitoringModule() {
    configureKoin()
    configureMonitoring(get())
}
