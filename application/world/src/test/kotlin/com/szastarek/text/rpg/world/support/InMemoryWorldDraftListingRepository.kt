package com.szastarek.text.rpg.world.support

import com.szastarek.text.rpg.acl.AccountId
import com.szastarek.text.rpg.world.draft.WorldDraftListItem
import com.szastarek.text.rpg.world.draft.WorldDraftListing
import com.szastarek.text.rpg.world.draft.WorldDraftListingRepository

class InMemoryWorldDraftListingRepository : WorldDraftListingRepository {
	private val db = mutableListOf<WorldDraftListItem>()

	override suspend fun findAllByAccountId(accountId: AccountId): WorldDraftListing {
		return WorldDraftListing(db.filter { it.ownerId == accountId })
	}

	fun add(item: WorldDraftListItem) {
		db.add(item)
	}

	fun clear() {
		db.clear()
	}
}
