package com.szastarek.text.rpg.world.draft.event

import com.szastarek.text.rpg.acl.AccountId
import com.szastarek.text.rpg.acl.SerializableAuthenticatedAccountContext
import com.szastarek.text.rpg.event.store.EventMetadata
import com.szastarek.text.rpg.event.store.EventMetadataBuilder
import com.szastarek.text.rpg.event.store.EventType
import com.szastarek.text.rpg.shared.Version
import com.szastarek.text.rpg.shared.Versioned
import com.szastarek.text.rpg.world.WorldName
import com.szastarek.text.rpg.world.draft.WorldDraft
import kotlinx.serialization.Contextual
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.litote.kmongo.Id

@Serializable
@SerialName("WorldDraftCreationRequestedEvent")
data class WorldDraftCreationRequestedEvent(
	@Contextual override val draftId: Id<WorldDraft>,
	val name: WorldName,
	val creatorAccountContext: SerializableAuthenticatedAccountContext,
) : WorldDraftEvent, Versioned {
	companion object {
		val eventType = EventType(WorldDraftEvent.eventCategory, "CreationRequested")
	}

	override val ownerId: AccountId = creatorAccountContext.accountId

	override fun getMetadata(causedBy: EventMetadata?): EventMetadata {
		return EventMetadataBuilder(
			aggregateId,
			WorldDraftEvent.eventCategory,
			eventType,
		).optionalCausedBy(causedBy).build()
	}

	override val version: Version = Version.initial
}
