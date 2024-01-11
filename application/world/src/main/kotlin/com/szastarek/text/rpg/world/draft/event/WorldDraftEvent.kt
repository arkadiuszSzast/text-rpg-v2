package com.szastarek.text.rpg.world.draft.event

import com.szastarek.text.rpg.acl.AccountId
import com.szastarek.text.rpg.event.store.AggregateId
import com.szastarek.text.rpg.event.store.DomainEvent
import com.szastarek.text.rpg.event.store.EventCategory
import com.szastarek.text.rpg.event.store.StreamName
import com.szastarek.text.rpg.world.draft.WorldDraft
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import org.litote.kmongo.Id

@Serializable
sealed interface WorldDraftEvent : DomainEvent {
	val draftId: Id<WorldDraft>
	val ownerId: AccountId

	companion object {
		val eventCategory: EventCategory
			get() = EventCategory("WorldDraft")

		fun aggregateStreamName(draftId: Id<WorldDraft>): StreamName = StreamName("${eventCategory.value}-$draftId")
	}
}

@Transient
val WorldDraftEvent.aggregateId: AggregateId
	get() = AggregateId(draftId.toString())
