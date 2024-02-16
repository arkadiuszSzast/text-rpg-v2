package com.szastarek.text.rpg.world.adapter.event.store

import arrow.core.Either
import arrow.core.getOrElse
import arrow.core.left
import arrow.core.right
import com.szastarek.text.rpg.acl.AccountId
import com.szastarek.text.rpg.event.store.EventStoreProjectionsClient
import com.szastarek.text.rpg.event.store.Partition
import com.szastarek.text.rpg.event.store.ProjectionOutdatedResult
import com.szastarek.text.rpg.event.store.ProjectionResultNotFound
import com.szastarek.text.rpg.event.store.ProjectionUpToDateResult
import com.szastarek.text.rpg.event.store.getResult
import com.szastarek.text.rpg.shared.Outdated
import com.szastarek.text.rpg.world.draft.WorldDraftListing
import com.szastarek.text.rpg.world.draft.WorldDraftListingRepository
import com.szastarek.text.rpg.world.draft.projection.WorldDraftListingByAccountIdProjectionCreator

class WorldDraftListingEventStoreRepository(
	private val projectionsClient: EventStoreProjectionsClient,
) : WorldDraftListingRepository {
	override suspend fun findAllByAccountId(accountId: AccountId): Either<Outdated<WorldDraftListing>, WorldDraftListing> {
		val result =
			projectionsClient.getResult<WorldDraftListing>(
				WorldDraftListingByAccountIdProjectionCreator.name,
				Partition(accountId.value),
			)
		return when (result) {
			is ProjectionUpToDateResult -> WorldDraftListing(result.data.drafts).right()
			is ProjectionOutdatedResult -> Outdated(WorldDraftListing(result.data.map { it.drafts }.getOrElse { emptyList() })).left()
			is ProjectionResultNotFound -> WorldDraftListing(emptyList()).right()
		}
	}
}
