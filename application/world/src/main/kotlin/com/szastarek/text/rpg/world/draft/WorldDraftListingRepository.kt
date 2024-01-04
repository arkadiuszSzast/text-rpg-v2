package com.szastarek.text.rpg.world.draft

import com.szastarek.text.rpg.acl.AccountId

interface WorldDraftListingRepository {
	suspend fun findAllByAccountId(accountId: AccountId): WorldDraftListing
}
