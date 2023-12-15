package com.szastarek.text.rpg.account.event

import com.szastarek.text.rpg.account.Account
import com.szastarek.text.rpg.event.store.AggregateId
import com.szastarek.text.rpg.event.store.DomainEvent
import com.szastarek.text.rpg.event.store.EventCategory
import com.szastarek.text.rpg.event.store.StreamName
import com.szastarek.text.rpg.shared.email.EmailAddress
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import org.litote.kmongo.Id

@Serializable
sealed interface AccountEvent : DomainEvent {
	val accountId: Id<Account>
	val emailAddress: EmailAddress

	companion object {
		val eventCategory: EventCategory
			get() = EventCategory("account")

		fun aggregateStreamName(emailAddress: EmailAddress): StreamName = StreamName("${eventCategory.value}-${emailAddress.value}")
	}
}

@Transient
val AccountEvent.aggregateId: AggregateId
	get() = AggregateId(emailAddress.value)
