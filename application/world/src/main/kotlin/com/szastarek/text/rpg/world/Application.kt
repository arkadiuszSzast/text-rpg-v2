package com.szastarek.text.rpg.world

import com.szastarek.text.rpg.event.store.eventStoreModule
import com.szastarek.text.rpg.mediator.mediatorModule
import com.szastarek.text.rpg.monitoring.monitoringModule
import com.szastarek.text.rpg.security.securityModule
import com.szastarek.text.rpg.shared.sharedModule
import com.szastarek.text.rpg.world.adapter.rest.configureWorldRouting
import com.szastarek.text.rpg.world.draft.subscriber.startWorldDraftCreationApprovingSubscriber
import com.szastarek.text.rpg.world.plugin.configureDocumentation
import com.szastarek.text.rpg.world.plugin.configureHealthz
import com.szastarek.text.rpg.world.plugin.configureKoin
import com.szastarek.text.rpg.world.plugin.createProjections
import io.ktor.server.application.Application
import org.koin.ktor.ext.get
import org.koin.ktor.ext.getKoin

fun main(args: Array<String>) {
	io.ktor.server.netty.EngineMain.main(args)
}

fun Application.main() {
	worldModule()
	getKoin().createEagerInstances()
}

fun Application.worldModule() {
	configureKoin()
	configureDocumentation(get())
	sharedModule()
	monitoringModule()
	securityModule()
	eventStoreModule()
	mediatorModule()
	createProjections(get())
	startWorldDraftCreationApprovingSubscriber(get())
	configureWorldRouting()
	configureHealthz()
}
