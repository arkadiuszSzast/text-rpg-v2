package com.szastarek.text.rpg.world.plugin

import com.szastarek.text.rpg.shared.config.ConfigKey
import com.szastarek.text.rpg.shared.config.getBooleanProperty
import com.szastarek.text.rpg.shared.plugin.installIfNotRegistered
import com.szastarek.text.rpg.world.adapter.event.store.WorldDraftListingEventStoreRepository
import com.szastarek.text.rpg.world.config.DocumentationProperties
import com.szastarek.text.rpg.world.draft.WorldDraftListingRepository
import com.szastarek.text.rpg.world.draft.command.handler.WorldDraftCreationRequestCommandHandler
import com.szastarek.text.rpg.world.draft.projection.WorldDraftListingByAccountIdProjectionCreator
import com.szastarek.text.rpg.world.draft.subscriber.WorldDraftCreationApprovingSubscriber
import io.ktor.server.application.Application
import org.koin.core.context.loadKoinModules
import org.koin.core.module.dsl.createdAtStart
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module
import org.koin.ktor.plugin.Koin

internal val worldConfigModule =
	module {
		single {
			DocumentationProperties(
				enabled = getBooleanProperty(ConfigKey("documentation.enabled")),
			)
		}
	}

internal val worldModule =
	module {
		singleOf(::WorldDraftCreationRequestCommandHandler)
		singleOf(::WorldDraftListingEventStoreRepository) bind WorldDraftListingRepository::class
		singleOf(::WorldDraftListingByAccountIdProjectionCreator) { createdAtStart() }
		singleOf(::WorldDraftCreationApprovingSubscriber) { createdAtStart() }
	}

internal fun Application.configureKoin() {
	installIfNotRegistered(Koin)
	loadKoinModules(listOf(worldModule, worldConfigModule))
}
