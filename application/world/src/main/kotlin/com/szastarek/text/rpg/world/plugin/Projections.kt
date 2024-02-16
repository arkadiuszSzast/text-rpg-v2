package com.szastarek.text.rpg.world.plugin

import com.szastarek.text.rpg.event.store.EventStoreProjectionsClient
import com.szastarek.text.rpg.shared.retry
import com.szastarek.text.rpg.world.draft.projection.WorldDraftListingByAccountIdProjectionCreator
import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktor.server.application.Application
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking

private val logger = KotlinLogging.logger {}

fun Application.createProjections(projectionsClient: EventStoreProjectionsClient) =
	runBlocking(Dispatchers.IO) {
		runCatching {
			retry {
				WorldDraftListingByAccountIdProjectionCreator(projectionsClient).createOrUpdateAndEnable()
			}
		}.onFailure {
			logger.error(it) { "Failed to create WorldDraftListingByAccountIdProjection" }
		}
	}
