package com.szastarek.text.rpg.event.store.utils

import com.szastarek.text.rpg.event.store.DomainEvent
import com.szastarek.text.rpg.event.store.EventCategory
import com.szastarek.text.rpg.event.store.EventMetadata
import com.szastarek.text.rpg.event.store.EventMetadataBuilder
import com.szastarek.text.rpg.event.store.EventType
import com.szastarek.text.rpg.event.store.asAggregateId
import com.szastarek.text.rpg.shared.Version
import com.szastarek.text.rpg.shared.Versioned
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import org.litote.kmongo.Id

@Serializable
data class Account(@Contextual val id: Id<Account>, val name: String)

@Serializable
data class AccountCreated(@Contextual val id: Id<Account>, val name: String) : DomainEvent, Versioned {
    override fun getMetadata(causedBy: EventMetadata?): EventMetadata {
        return EventMetadataBuilder(
            id.asAggregateId(),
            EventCategory("account"),
            EventType("account-created")
        ).optionalCausedBy(causedBy).build()
    }

    override val version: Version = Version.initial
}

@Serializable
data class AccountNameUpdated(@Contextual val id: Id<Account>, val name: String, override val version: Version) :
    DomainEvent, Versioned {
    override fun getMetadata(causedBy: EventMetadata?): EventMetadata {
        return EventMetadataBuilder(
            id.asAggregateId(),
            EventCategory("account"),
            EventType("account-name-updated")
        ).optionalCausedBy(causedBy).build()
    }
}

@Serializable
data class Email(@Contextual val id: Id<Email>)

@Serializable
data class EmailSent(@Contextual val id: Id<Email>) : DomainEvent {
    override fun getMetadata(causedBy: EventMetadata?): EventMetadata {
        return EventMetadataBuilder(
            id.asAggregateId(),
            EventCategory("email"),
            EventType("email-sent")
        ).optionalCausedBy(causedBy).build()
    }
}
