package com.szastarek.text.rpg.account

import com.szastarek.text.rpg.account.adapter.rest.configureAccountRouting
import com.szastarek.text.rpg.account.plugin.configureKoin
import com.szastarek.text.rpg.documentation.documentationModule
import com.szastarek.text.rpg.event.store.eventStoreModule
import com.szastarek.text.rpg.mail.mailModule
import com.szastarek.text.rpg.mediator.mediatorModule
import com.szastarek.text.rpg.monitoring.monitoringModule
import com.szastarek.text.rpg.redis.redisModule
import com.szastarek.text.rpg.security.securityModule
import com.szastarek.text.rpg.shared.sharedModule
import io.ktor.server.application.Application

fun Application.accountModule() {
    configureKoin()
    sharedModule()
    monitoringModule()
    securityModule()
    documentationModule()
    eventStoreModule()
    mediatorModule()
    mailModule()
    redisModule()
    configureAccountRouting()
}
