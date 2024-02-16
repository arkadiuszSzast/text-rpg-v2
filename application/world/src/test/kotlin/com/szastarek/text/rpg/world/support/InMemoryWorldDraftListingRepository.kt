package com.szastarek.text.rpg.world.support

import arrow.core.Either
import arrow.core.right
import com.szastarek.text.rpg.acl.AccountId
import com.szastarek.text.rpg.shared.Outdated
import com.szastarek.text.rpg.world.draft.WorldDraftListItem
import com.szastarek.text.rpg.world.draft.WorldDraftListing
import com.szastarek.text.rpg.world.draft.WorldDraftListingRepository

class InMemoryWorldDraftListingRepository : WorldDraftListingRepository {
	private val db = mutableListOf<WorldDraftListItem>()

	override suspend fun findAllByAccountId(accountId: AccountId): Either<Outdated<WorldDraftListing>, WorldDraftListing> {
		return WorldDraftListing(db.filter { it.ownerId == accountId }).right()
	}

	fun add(item: WorldDraftListItem) {
		db.add(item)
	}

	fun clear() {
		db.clear()
	}
}
