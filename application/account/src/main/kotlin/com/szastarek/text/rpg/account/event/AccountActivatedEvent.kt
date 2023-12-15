package com.szastarek.text.rpg.account.event

import com.szastarek.text.rpg.account.Account
import com.szastarek.text.rpg.event.store.EventMetadata
import com.szastarek.text.rpg.event.store.EventMetadataBuilder
import com.szastarek.text.rpg.event.store.EventType
import com.szastarek.text.rpg.shared.Version
import com.szastarek.text.rpg.shared.Versioned
import com.szastarek.text.rpg.shared.email.EmailAddress
import kotlinx.serialization.Contextual
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.litote.kmongo.Id

@Serializable
@SerialName("AccountActivatedEvent")
data class AccountActivatedEvent(
	@Contextual override val accountId: Id<Account>,
	override val emailAddress: EmailAddress,
	override val version: Version,
) : AccountEvent, Versioned {
	companion object {
		val eventType = EventType(AccountEvent.eventCategory, "activated")
	}

	override fun getMetadata(causedBy: EventMetadata?): EventMetadata {
		return EventMetadataBuilder(
			aggregateId,
			AccountEvent.eventCategory,
			eventType,
		).optionalCausedBy(causedBy).build()
	}
}
