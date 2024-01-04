package com.szastarek.text.rpg.world.plugin

import com.szastarek.text.rpg.shared.plugin.installIfNotRegistered
import com.szastarek.text.rpg.world.adapter.event.store.WorldDraftListingEventStoreRepository
import com.szastarek.text.rpg.world.draft.WorldDraftListingRepository
import com.szastarek.text.rpg.world.draft.command.handler.WorldDraftCreationRequestCommandHandler
import com.szastarek.text.rpg.world.draft.projection.WorldDraftListingByAccountIdProjection
import io.ktor.server.application.Application
import org.koin.core.context.GlobalContext
import org.koin.core.context.loadKoinModules
import org.koin.core.module.dsl.createdAtStart
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module
import org.koin.ktor.plugin.Koin

internal val worldModule =
	module {
		singleOf(::WorldDraftCreationRequestCommandHandler)
		singleOf(::WorldDraftListingEventStoreRepository) bind WorldDraftListingRepository::class
		singleOf(::WorldDraftListingByAccountIdProjection) { createdAtStart() }
	}

internal fun Application.configureKoin() {
	installIfNotRegistered(Koin) {
		// TODO remove when Koin 3.5.2 would be released
		GlobalContext.startKoin(this)
	}
	loadKoinModules(worldModule)
}
