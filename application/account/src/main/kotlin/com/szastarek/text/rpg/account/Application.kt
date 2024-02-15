package com.szastarek.text.rpg.account

import com.szastarek.text.rpg.account.adapter.rest.configureAccountRouting
import com.szastarek.text.rpg.account.plugin.configureDocumentation
import com.szastarek.text.rpg.account.plugin.configureHealthz
import com.szastarek.text.rpg.account.plugin.configureKoin
import com.szastarek.text.rpg.event.store.eventStoreModule
import com.szastarek.text.rpg.mail.mailModule
import com.szastarek.text.rpg.mediator.mediatorModule
import com.szastarek.text.rpg.monitoring.monitoringModule
import com.szastarek.text.rpg.redis.redisModule
import com.szastarek.text.rpg.security.securityModule
import com.szastarek.text.rpg.shared.sharedModule
import io.ktor.server.application.Application
import org.koin.ktor.ext.get
import org.koin.ktor.ext.getKoin

fun main(args: Array<String>) {
	io.ktor.server.netty.EngineMain.main(args)
}

fun Application.main() {
	accountModule()
	getKoin().createEagerInstances()
}

fun Application.accountModule() {
	configureKoin()
	configureDocumentation(get())
	sharedModule()
	monitoringModule()
	securityModule()
	eventStoreModule()
	mediatorModule()
	mailModule()
	redisModule()
	configureAccountRouting()
	configureHealthz()
}
