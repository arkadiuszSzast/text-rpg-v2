package com.szastarek.text.rpg.world.draft.command.handler

import arrow.core.raise.either
import com.szastarek.text.rpg.acl.authority.AuthorizedAccountAbilityProvider
import com.szastarek.text.rpg.event.store.EventStoreWriteClient
import com.szastarek.text.rpg.event.store.appendToStream
import com.szastarek.text.rpg.event.store.revision
import com.szastarek.text.rpg.world.draft.WorldDraftAggregate
import com.szastarek.text.rpg.world.draft.WorldDraftListingRepository
import com.szastarek.text.rpg.world.draft.command.WorldDraftCreationRequestCommand
import com.szastarek.text.rpg.world.draft.command.WorldDraftCreationRequestCommandResult
import com.szastarek.text.rpg.world.draft.command.WorldDraftCreationRequestCommandSuccessResult
import com.szastarek.text.rpg.world.draft.event.WorldDraftEvent
import com.trendyol.kediatr.CommandWithResultHandler

class WorldDraftCreationRequestCommandHandler(
	private val acl: AuthorizedAccountAbilityProvider,
	private val eventStoreWriteClient: EventStoreWriteClient,
	private val worldDraftListingRepository: WorldDraftListingRepository,
) : CommandWithResultHandler<WorldDraftCreationRequestCommand, WorldDraftCreationRequestCommandResult> {
	override suspend fun handle(command: WorldDraftCreationRequestCommand): WorldDraftCreationRequestCommandResult =
		either {
			acl.ensuring().ensureCanCreateInstanceOf(WorldDraftAggregate.aclResourceIdentifier)

			val (name, accountContext) = command

			val existingDrafts = worldDraftListingRepository.findAllByAccountId(accountContext.accountId)
			val event = WorldDraftAggregate.initializeCreation(accountContext, name, existingDrafts.drafts).bind()

			eventStoreWriteClient.appendToStream<WorldDraftEvent>(event, event.revision())

			WorldDraftCreationRequestCommandSuccessResult(event.draftId)
		}
}
