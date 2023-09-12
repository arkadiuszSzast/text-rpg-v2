package com.szastarek.text.rpg.account

import com.szastarek.text.rpg.account.adapter.rest.configureAccountRouting
import com.szastarek.text.rpg.account.plugin.configureKoin
import com.szastarek.text.rpg.documentation.documentationModule
import com.szastarek.text.rpg.event.store.eventStoreModule
import com.szastarek.text.rpg.monitoring.monitoringModule
import com.szastarek.text.rpg.security.securityModule
import com.szastarek.text.rpg.shared.sharedModule
import io.ktor.server.application.Application
import org.koin.ktor.ext.getKoin

fun Application.accountModule() {
    configureKoin()
    sharedModule()
    monitoringModule()
    securityModule()
    documentationModule()
    eventStoreModule()
    configureAccountRouting()
    getKoin().createEagerInstances()
}
