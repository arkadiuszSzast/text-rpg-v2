package com.szastarek.text.rpg.account

import arrow.core.Nel
import arrow.core.raise.either
import arrow.core.raise.ensure
import arrow.core.raise.zipOrAccumulate
import com.szastarek.text.rpg.account.command.LogInAccountError
import com.szastarek.text.rpg.account.event.AccountCreatedEvent
import com.szastarek.text.rpg.acl.Role
import com.szastarek.text.rpg.acl.authority.Authority
import com.szastarek.text.rpg.shared.email.EmailAddress
import com.szastarek.text.rpg.shared.password.HashedPassword
import com.szastarek.text.rpg.shared.password.RawPassword
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import org.litote.kmongo.Id
import org.litote.kmongo.newId

data class AccountAggregate(
  override val id: Id<Account>,
  override val emailAddress: EmailAddress,
  val status: AccountStatus,
  val role: Role,
  val customAuthorities: List<Authority>,
  val password: HashedPassword,
  val createdAt: Instant,
  val timeZone: TimeZone
) : Account {
  companion object {
    fun create(emailAddress: EmailAddress, role: Role, password: HashedPassword, timeZone: TimeZone, clock: Clock) =
      AccountCreatedEvent(
        newId(),
        emailAddress,
        AccountStatus.Staged,
        role,
        emptyList(),
        password,
        clock.now(),
        timeZone
      )
  }

  fun logIn(logInRequestPassword: RawPassword) = either<Nel<LogInAccountError>, LoginSuccess> {
    zipOrAccumulate(
      { ensure(password.matches(logInRequestPassword)) { LogInAccountError.InvalidPassword } },
      { ensure(status == AccountStatus.Active) { LogInAccountError.AccountNotActive } },
      { _, _ -> LoginSuccess }
    )
  }
}

object LoginSuccess
