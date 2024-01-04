package com.szastarek.text.rpg.world.draft

import arrow.core.nel
import arrow.core.raise.either
import arrow.core.raise.ensure
import com.szastarek.text.rpg.acl.AccountId
import com.szastarek.text.rpg.acl.AccountIdProvider
import com.szastarek.text.rpg.world.WorldDescription
import com.szastarek.text.rpg.world.WorldName
import com.szastarek.text.rpg.world.draft.command.WorldDraftCreationRequestError
import com.szastarek.text.rpg.world.draft.event.WorldDraftCreatedEvent
import com.szastarek.text.rpg.world.draft.event.WorldDraftCreationRequestedEvent
import org.litote.kmongo.Id
import org.litote.kmongo.newId

data class WorldDraftAggregate(
    val id: Id<WorldDraft>,
    val ownerId: AccountId,
    val name: WorldName,
    val description: WorldDescription? = null,
) {
    companion object {
        fun initializeCreation(
            accountIdProvider: AccountIdProvider,
            name: WorldName,
            existingDrafts: List<WorldDraftListItem>,
        ) = either {
            ensure(existingDrafts.size <= 3) { WorldDraftCreationRequestError.MaximumNumberOfDraftsReached.nel() }
            WorldDraftCreationRequestedEvent(newId(), name, accountIdProvider.accountId)
        }

        fun create(
            accountIdProvider: AccountIdProvider,
            name: WorldName,
            description: WorldDescription?,
        ) = WorldDraftCreatedEvent(newId(), name, description, accountIdProvider.accountId)
    }
}
