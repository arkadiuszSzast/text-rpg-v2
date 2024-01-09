package com.szastarek.text.rpg.world.draft.event

import com.szastarek.text.rpg.event.store.EventMetadata
import com.szastarek.text.rpg.event.store.EventMetadataBuilder
import com.szastarek.text.rpg.event.store.EventType
import com.szastarek.text.rpg.shared.Version
import com.szastarek.text.rpg.shared.Versioned
import com.szastarek.text.rpg.world.draft.WorldDraft
import kotlinx.serialization.Contextual
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.litote.kmongo.Id

@Serializable
@SerialName("WorldDraftCreationApprovedEvent")
data class WorldDraftCreationApprovedEvent(
	@Contextual override val draftId: Id<WorldDraft>,
	override val version: Version,
) : WorldDraftEvent, Versioned {
	companion object {
		val eventType = EventType(WorldDraftEvent.eventCategory, "CreationApproved")
	}

	override fun getMetadata(causedBy: EventMetadata?): EventMetadata {
		return EventMetadataBuilder(
			aggregateId,
			WorldDraftEvent.eventCategory,
			eventType,
		).optionalCausedBy(causedBy).build()
	}
}
