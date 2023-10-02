package com.szastarek.text.rpg.account.support

import com.szastarek.text.rpg.account.Account
import com.szastarek.text.rpg.account.AccountAggregate
import com.szastarek.text.rpg.account.AccountStatus
import com.szastarek.text.rpg.account.adapter.rest.request.CreateAccountRequest
import com.szastarek.text.rpg.account.adapter.rest.request.CreateWorldCreatorAccountRequest
import com.szastarek.text.rpg.account.command.CreateRegularAccountCommand
import com.szastarek.text.rpg.account.command.LogInAccountCommand
import com.szastarek.text.rpg.account.event.AccountCreatedEvent
import com.szastarek.text.rpg.acl.Role
import com.szastarek.text.rpg.acl.Roles
import com.szastarek.text.rpg.acl.authority.Authority
import com.szastarek.text.rpg.security.JwtToken
import com.szastarek.text.rpg.shared.MaskedString
import com.szastarek.text.rpg.shared.Version
import com.szastarek.text.rpg.shared.aRawPassword
import com.szastarek.text.rpg.shared.anEmail
import com.szastarek.text.rpg.shared.email.EmailAddress
import com.szastarek.text.rpg.shared.password.HashedPassword
import com.szastarek.text.rpg.shared.password.RawPassword
import io.github.serpro69.kfaker.faker
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import org.litote.kmongo.Id
import org.litote.kmongo.newId


private val faker = faker {}

fun aCreateAccountRequest(
  email: String = faker.internet.email(),
  password: String = faker.random.randomString(8),
  timeZone: String = TimeZone.availableZoneIds.random()
) = CreateAccountRequest(
  email,
  MaskedString(password),
  timeZone
)

fun aCreateRegularAccountCommand(
  email: EmailAddress = anEmail(),
  password: RawPassword = aRawPassword(),
  timeZone: TimeZone = TimeZone.of(TimeZone.availableZoneIds.random())
) = CreateRegularAccountCommand(
  email,
  password.hashpw(),
  timeZone
)

fun aCreateWorldCreatorAccountRequest(
  email: String = faker.internet.email(),
  password: String = faker.random.randomString(8),
  timeZone: TimeZone = TimeZone.of(TimeZone.availableZoneIds.random()),
  token: JwtToken
) = CreateWorldCreatorAccountRequest(
  email,
  password,
  timeZone.id,
  token.value
)

fun anAccountCreatedEvent(
  accountId: Id<Account> = newId(),
  email: EmailAddress = anEmail(),
  status: AccountStatus = faker.random.nextEnum(),
  role: Role = faker.random.nextEnum<Roles>().role,
  customAuthorities: List<Authority> = emptyList(),
  password: RawPassword = aRawPassword(),
  createdAt: Instant = Clock.System.now(),
  timeZone: TimeZone = TimeZone.of(TimeZone.availableZoneIds.random())
) = AccountCreatedEvent(
  accountId,
  email,
  status,
  role,
  customAuthorities,
  password.hashpw(),
  createdAt,
  timeZone
)

fun aLogInAccountCommand(
  email: EmailAddress = anEmail(),
  password: RawPassword = aRawPassword()
) = LogInAccountCommand(
  email,
  password
)

fun anAccountAggregate(
  id: Id<Account> = newId(),
  emailAddress: EmailAddress = anEmail(),
  status: AccountStatus = faker.random.nextEnum(),
  role: Role = faker.random.nextEnum<Roles>().role,
  customAuthorities: List<Authority> = emptyList(),
  password: HashedPassword = aRawPassword().hashpw(),
  createdAt: Instant = Clock.System.now(),
  timeZone: TimeZone = TimeZone.of(TimeZone.availableZoneIds.random()),
  version: Version = Version(faker.random.nextLong(10))
) = AccountAggregate(
  id = id,
  emailAddress = emailAddress,
  status = status,
  role = role,
  customAuthorities = customAuthorities,
  password = password,
  createdAt = createdAt,
  timeZone = timeZone,
  version = version
)