package com.szastarek.text.rpg.world

import com.szastarek.text.rpg.documentation.documentationModule
import com.szastarek.text.rpg.event.store.eventStoreModule
import com.szastarek.text.rpg.mediator.mediatorModule
import com.szastarek.text.rpg.monitoring.monitoringModule
import com.szastarek.text.rpg.security.securityModule
import com.szastarek.text.rpg.shared.sharedModule
import com.szastarek.text.rpg.world.adapter.rest.configureWorldRouting
import com.szastarek.text.rpg.world.plugin.configureDocumentation
import com.szastarek.text.rpg.world.plugin.configureKoin
import io.ktor.server.application.Application
import org.koin.ktor.ext.get
import org.koin.ktor.ext.getKoin

fun main(args: Array<String>) {
	io.ktor.server.netty.EngineMain.main(args)
}

fun Application.main() {
	configureKoin()
	configureDocumentation(get())
	sharedModule()
	monitoringModule()
	securityModule()
	documentationModule()
	eventStoreModule()
	mediatorModule()
	configureWorldRouting()
	getKoin().createEagerInstances()
}
