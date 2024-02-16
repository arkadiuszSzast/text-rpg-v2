package com.szastarek.text.rpg.world.draft

import arrow.core.Either
import com.szastarek.text.rpg.acl.AccountId
import com.szastarek.text.rpg.shared.Outdated

interface WorldDraftListingRepository {
	suspend fun findAllByAccountId(accountId: AccountId): Either<Outdated<WorldDraftListing>, WorldDraftListing>
}
