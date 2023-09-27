package com.szastarek.text.rpg.account.event

import com.szastarek.text.rpg.account.Account
import com.szastarek.text.rpg.account.AccountStatus
import com.szastarek.text.rpg.acl.Role
import com.szastarek.text.rpg.acl.authority.Authority
import com.szastarek.text.rpg.event.store.EventType
import com.szastarek.text.rpg.event.store.EventMetadata
import com.szastarek.text.rpg.event.store.EventMetadataBuilder
import com.szastarek.text.rpg.shared.Version
import com.szastarek.text.rpg.shared.Versioned
import com.szastarek.text.rpg.shared.email.EmailAddress
import com.szastarek.text.rpg.shared.password.HashedPassword
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.serialization.Contextual
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.litote.kmongo.Id

@Serializable
@SerialName("AccountCreatedEvent")
data class AccountCreatedEvent(
  @Contextual override val accountId: Id<Account>,
  override val emailAddress: EmailAddress,
  val status: AccountStatus,
  val role: Role,
  val customAuthorities: List<Authority>,
  val password: HashedPassword,
  val createdAt: Instant,
  val timeZone: TimeZone
) : AccountEvent, Versioned {

  companion object {
    val eventType = EventType(AccountEvent.eventCategory, "created")
  }
  override val version: Version
    get() = Version.initial

  override fun getMetadata(causedBy: EventMetadata?): EventMetadata {
    return EventMetadataBuilder(
      aggregateId,
      AccountEvent.eventCategory,
      eventType
    ).optionalCausedBy(causedBy).build()
  }
}
