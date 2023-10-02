package com.szastarek.text.rpg.account

import arrow.core.Either
import arrow.core.NonEmptyList
import arrow.core.raise.either
import arrow.core.raise.ensureNotNull
import arrow.core.raise.zipOrAccumulate
import com.szastarek.text.rpg.account.event.AccountActivatedEvent
import com.szastarek.text.rpg.account.event.AccountCreatedEvent
import com.szastarek.text.rpg.account.event.AccountEvent
import com.szastarek.text.rpg.account.event.AccountPasswordChangedEvent
import com.szastarek.text.rpg.acl.Role
import com.szastarek.text.rpg.acl.authority.Authority
import com.szastarek.text.rpg.shared.Version
import com.szastarek.text.rpg.shared.email.EmailAddress
import com.szastarek.text.rpg.shared.password.HashedPassword
import com.szastarek.text.rpg.shared.validate.ValidationError
import com.szastarek.text.rpg.shared.validate.ValidationErrors
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import org.litote.kmongo.Id


class AccountAggregateBuilder {
  private var accountId: Id<Account>? = null
  private var emailAddress: EmailAddress? = null
  private var status: AccountStatus? = null
  private var role: Role? = null
  private var customAuthorities: List<Authority>? = null
  private var password: HashedPassword? = null
  private var createdAt: Instant? = null
  private var timeZone: TimeZone? = null
  private var version: Version? = null

  fun apply(events: NonEmptyList<AccountEvent>): Either<ValidationErrors, AccountAggregate> {
    return events.fold(this) { acc, accountEvent -> acc.apply(accountEvent) }.build()
  }

  private fun apply(event: AccountEvent): AccountAggregateBuilder {
    return when(event) {
      is AccountCreatedEvent -> applyAccountCreated(event)
      is AccountActivatedEvent -> applyAccountActivatedEvent(event)
      is AccountPasswordChangedEvent -> applyAccountPasswordChangedEvent(event)
    }
  }

  private fun applyAccountCreated(accountCreatedEvent: AccountCreatedEvent): AccountAggregateBuilder {
    return this.apply {
      accountId = accountCreatedEvent.accountId
      emailAddress = accountCreatedEvent.emailAddress
      status = accountCreatedEvent.status
      role = accountCreatedEvent.role
      customAuthorities = accountCreatedEvent.customAuthorities
      password = accountCreatedEvent.password
      createdAt = accountCreatedEvent.createdAt
      timeZone = accountCreatedEvent.timeZone
      version = accountCreatedEvent.version
    }
  }

  private fun applyAccountActivatedEvent(accountActivatedEvent: AccountActivatedEvent): AccountAggregateBuilder {
    return this.apply {
      status = AccountStatus.Active
      version = accountActivatedEvent.version
    }
  }

  private fun applyAccountPasswordChangedEvent(accountPasswordChangedEvent: AccountPasswordChangedEvent): AccountAggregateBuilder {
    return this.apply {
      password = accountPasswordChangedEvent.password
      version = accountPasswordChangedEvent.version
    }
  }

  private fun build() = either<ValidationErrors, AccountAggregate> {
    zipOrAccumulate(
      { ensureNotNull(accountId) { ValidationError(".accountId", "account_id_null") } },
      { ensureNotNull(emailAddress) { ValidationError(".emailAddress", "email_address_null") } },
      { ensureNotNull(status) { ValidationError(".status", "status_null") } },
      { ensureNotNull(role) { ValidationError(".role", "role_null") } },
      { ensureNotNull(customAuthorities) { ValidationError(".custom_authorities", "custom_authorities_null") } },
      { ensureNotNull(password) { ValidationError(".password", "password_null") } },
      { ensureNotNull(createdAt) { ValidationError(".createdAt", "created_at_null") } },
      { ensureNotNull(timeZone) { ValidationError(".timeZone", "time_zone_null") } },
      { ensureNotNull(version) { ValidationError(".version", "version_null") } },
      { accountIdN, emailAddressN, statusN, roleN, customAuthoritiesN, passwordN, createdAtN, timeZoneN, versionN ->
        AccountAggregate(
          accountIdN,
          emailAddressN,
          statusN,
          roleN,
          customAuthoritiesN,
          passwordN,
          createdAtN,
          timeZoneN,
          versionN
        )
      }
    )
  }
}
