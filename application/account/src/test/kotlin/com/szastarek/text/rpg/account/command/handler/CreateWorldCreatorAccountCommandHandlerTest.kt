package com.szastarek.text.rpg.account.command.handler

import com.szastarek.text.rpg.account.Account
import com.szastarek.text.rpg.account.AccountStatus
import com.szastarek.text.rpg.account.adapter.event.store.AccountAggregateEventStoreRepository
import com.szastarek.text.rpg.account.command.CreateWorldCreatorAccountCommand
import com.szastarek.text.rpg.account.command.CreateWorldCreatorAccountError
import com.szastarek.text.rpg.account.config.WorldCreatorRegisterProperties
import com.szastarek.text.rpg.account.event.AccountCreatedEvent
import com.szastarek.text.rpg.account.event.AccountEvent
import com.szastarek.text.rpg.account.support.anAccountCreatedEvent
import com.szastarek.text.rpg.account.world.creator.RegisterWorldCreatorTokenVerifier
import com.szastarek.text.rpg.account.world.creator.WorldCreatorRegisterUrlProvider
import com.szastarek.text.rpg.acl.Roles
import com.szastarek.text.rpg.event.store.InMemoryEventStore
import com.szastarek.text.rpg.security.JwtIssuer
import com.szastarek.text.rpg.security.JwtProperties
import com.szastarek.text.rpg.security.JwtSecret
import com.szastarek.text.rpg.security.JwtToken
import com.szastarek.text.rpg.shared.aRawPassword
import com.szastarek.text.rpg.shared.anEmail
import com.szastarek.text.rpg.utils.FixedClock
import io.kotest.assertions.arrow.core.shouldBeLeft
import io.kotest.assertions.arrow.core.shouldBeRight
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.should
import io.kotest.matchers.shouldBe
import io.ktor.http.Url
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import org.litote.kmongo.Id
import kotlin.time.Duration.Companion.minutes

class CreateWorldCreatorAccountCommandHandlerTest : DescribeSpec() {

  private val clock = FixedClock(Clock.System.now())
  private val eventStore = InMemoryEventStore()
  private val accountAggregateRepository = AccountAggregateEventStoreRepository(eventStore)
  private val worldCreatorRegisterProperties = WorldCreatorRegisterProperties(
    registerUrl = Url("http://test-host:3000/account/world-creator"),
    jwtConfig = JwtProperties(
      JwtSecret("world-creator-register-jwt-test-secret"),
      JwtIssuer("world-creator-register-jwt-test-issuer"),
      15.minutes
    )
  )
  private val worldCreatorRegisterUrlProvider = WorldCreatorRegisterUrlProvider(worldCreatorRegisterProperties, clock)
  private val registerWorldCreatorTokenVerifier = RegisterWorldCreatorTokenVerifier(worldCreatorRegisterProperties)
  private val handler = CreateWorldCreatorAccountCommandHandler(
    registerWorldCreatorTokenVerifier,
    accountAggregateRepository,
    eventStore,
    clock
  )

  init {

    describe("CreateWorldCreatorAccountCommandHandlerTest") {

      beforeTest { eventStore.clear() }

      it("should create world creator account") {
        //arrange
        val email = anEmail()
        val token = JwtToken(worldCreatorRegisterUrlProvider.provide(email).parameters["token"]!!)
        val command = CreateWorldCreatorAccountCommand(email, aRawPassword().hashpw(), TimeZone.UTC, token)

        //act
        val result = handler.handle(command)

        //assert
        val appendedEvent = eventStore.readStreamByEventType(AccountCreatedEvent.eventType, AccountCreatedEvent::class).single()
        result.shouldBeRight().should {
          val expectedEvent = command.toExpectedEvent(it.accountId)
          appendedEvent shouldBe expectedEvent
        }
      }

      it("cannot register world creator account on different email than from subject") {
        //arrange
        val email = anEmail()
        val differentEmail = anEmail()
        val token = JwtToken(worldCreatorRegisterUrlProvider.provide(email).parameters["token"]!!)
        val command = CreateWorldCreatorAccountCommand(differentEmail, aRawPassword().hashpw(), TimeZone.UTC, token)

        //act
        val result = handler.handle(command)

        //assert
        eventStore.readStreamByEventType(AccountCreatedEvent.eventType, AccountCreatedEvent::class).shouldBeEmpty()
        result.shouldBeLeft(listOf(CreateWorldCreatorAccountError.InvalidToken))
      }

      it("should not create world creator account when email is already taken") {
        //arrange
        val email = anEmail()
        val alreadyExistingAccount = anAccountCreatedEvent(email = email, role = Roles.RegularUser.role)
          .also { eventStore.appendToStream(it, AccountEvent::class) }
        val token = JwtToken(worldCreatorRegisterUrlProvider.provide(email).parameters["token"]!!)
        val command = CreateWorldCreatorAccountCommand(
          alreadyExistingAccount.emailAddress,
          aRawPassword().hashpw(),
          TimeZone.UTC,
          token
        )

        //act
        val result = handler.handle(command)

        //assert
        eventStore.readStreamByEventType(AccountCreatedEvent.eventType, AccountCreatedEvent::class).single().should {
          it.role shouldBe alreadyExistingAccount.role
        }
        result.shouldBeLeft(listOf(CreateWorldCreatorAccountError.EmailAlreadyTaken))
      }
    }

  }

  private fun CreateWorldCreatorAccountCommand.toExpectedEvent(id: Id<Account>) = AccountCreatedEvent(
    id,
    email,
    AccountStatus.Staged,
    Roles.WorldCreator.role,
    emptyList(),
    password,
    clock.now(),
    timeZoneId
  )
}
