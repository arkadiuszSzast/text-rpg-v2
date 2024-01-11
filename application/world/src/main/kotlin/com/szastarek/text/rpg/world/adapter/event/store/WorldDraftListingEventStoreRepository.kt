package com.szastarek.text.rpg.world.adapter.event.store

import arrow.core.getOrElse
import com.szastarek.text.rpg.acl.AccountId
import com.szastarek.text.rpg.event.store.EventStoreProjectionsClient
import com.szastarek.text.rpg.event.store.Partition
import com.szastarek.text.rpg.event.store.getResult
import com.szastarek.text.rpg.world.draft.WorldDraftListing
import com.szastarek.text.rpg.world.draft.WorldDraftListingRepository
import com.szastarek.text.rpg.world.draft.projection.WorldDraftListingByAccountIdProjectionCreator

class WorldDraftListingEventStoreRepository(
	private val projectionsClient: EventStoreProjectionsClient,
) : WorldDraftListingRepository {
	override suspend fun findAllByAccountId(accountId: AccountId): WorldDraftListing {
		return projectionsClient.getResult<WorldDraftListing>(
			WorldDraftListingByAccountIdProjectionCreator.name,
			Partition(accountId.value),
		).getOrElse { WorldDraftListing(emptyList()) }
	}
}
