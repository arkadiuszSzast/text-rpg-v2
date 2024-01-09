package com.szastarek.text.rpg.world.draft

import arrow.core.raise.either
import com.szastarek.text.rpg.acl.AccountId
import com.szastarek.text.rpg.acl.AclResource
import com.szastarek.text.rpg.acl.AclResourceIdentifier
import com.szastarek.text.rpg.acl.AuthenticatedAccountContext
import com.szastarek.text.rpg.acl.BelongsToAccount
import com.szastarek.text.rpg.acl.serializable
import com.szastarek.text.rpg.world.WorldDescription
import com.szastarek.text.rpg.world.WorldName
import com.szastarek.text.rpg.world.draft.event.WorldDraftCreationApprovedEvent
import com.szastarek.text.rpg.world.draft.event.WorldDraftCreationRejectedEvent
import com.szastarek.text.rpg.world.draft.event.WorldDraftCreationRequestedEvent
import org.litote.kmongo.Id
import org.litote.kmongo.newId

data class WorldDraftAggregate(
	val id: Id<WorldDraft>,
	val ownerId: AccountId,
	val name: WorldName,
	val description: WorldDescription? = null,
) : AclResource, BelongsToAccount {
	override val aclResourceIdentifier: AclResourceIdentifier
		get() = WorldDraftAggregate.aclResourceIdentifier

	override val accountId: AccountId
		get() = ownerId

	companion object {
		val aclResourceIdentifier = AclResourceIdentifier("world-draft-aggregate")

		suspend fun initializeCreation(
			creatorAccountContext: AuthenticatedAccountContext,
			name: WorldName,
			existingDrafts: List<WorldDraftListItem>,
		) = either {
			WorldDraftCreatePolicy.get().isAllowed(creatorAccountContext, existingDrafts).bind()

			WorldDraftCreationRequestedEvent(newId(), name, creatorAccountContext.serializable())
		}

		fun create(
			worldDraftCreationRequestedEvent: WorldDraftCreationRequestedEvent,
			existingDrafts: List<WorldDraftListItem>,
		) = either {
			WorldDraftCreatePolicy.get()
				.isAllowed(worldDraftCreationRequestedEvent.creatorAccountContext, existingDrafts)
				.mapLeft {
					WorldDraftCreationRejectedEvent(
						worldDraftCreationRequestedEvent.draftId,
						it,
						worldDraftCreationRequestedEvent.version.next(),
					)
				}.bind()

			WorldDraftCreationApprovedEvent(
				worldDraftCreationRequestedEvent.draftId,
				worldDraftCreationRequestedEvent.version.next(),
			)
		}
	}
}
