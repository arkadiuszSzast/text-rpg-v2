package com.szastarek.text.rpg.account.command.handler

import arrow.core.nonEmptyListOf
import com.szastarek.text.rpg.account.AccountStatus
import com.szastarek.text.rpg.account.adapter.event.store.AccountAggregateEventStoreRepository
import com.szastarek.text.rpg.account.command.LogInAccountError
import com.szastarek.text.rpg.account.event.AccountEvent
import com.szastarek.text.rpg.account.support.InMemoryRefreshTokenRepository
import com.szastarek.text.rpg.account.support.aLogInAccountCommand
import com.szastarek.text.rpg.account.support.anAccountCreatedEvent
import com.szastarek.text.rpg.event.store.InMemoryEventStore
import com.szastarek.text.rpg.security.AuthTokenProvider
import com.szastarek.text.rpg.security.config.AuthenticationProperties
import com.szastarek.text.rpg.shared.aRawPassword
import com.szastarek.text.rpg.utils.FixedClock
import io.kotest.assertions.arrow.core.shouldBeLeft
import io.kotest.assertions.arrow.core.shouldBeRight
import io.kotest.core.spec.style.DescribeSpec
import kotlin.time.Duration.Companion.hours

class LogInAccountCommandHandlerTest : DescribeSpec() {

  private val clock = FixedClock()
  private val eventStore = InMemoryEventStore()
  private val accountAggregateRepository = AccountAggregateEventStoreRepository(eventStore)
  private val authenticationProperties = AuthenticationProperties(
    jwtAudience = "test-audience",
    jwtIssuer = "test-issuer",
    jwtRealm = "test-realm",
    jwtSecret = "test-secret",
    authTokenExpiration = 1.hours,
  )
  private val authTokenProvider = AuthTokenProvider(authenticationProperties, clock)
  private val refreshTokenRepository = InMemoryRefreshTokenRepository()
  private val handler = LogInAccountCommandHandler(accountAggregateRepository, authTokenProvider, refreshTokenRepository)

  init {

    describe("LogInAccountCommandHandlerTest") {

      beforeTest {
        refreshTokenRepository.clear()
        eventStore.clear()
      }

      it("should fail when account not found") {
        //arrange
        val command = aLogInAccountCommand()

        //act
        val result = handler.handle(command)

        //assert
        result.shouldBeLeft(nonEmptyListOf(LogInAccountError.AccountNotFound))
      }

      it("should fail when password does not match") {
        //arrange
        val password = aRawPassword()
        val invalidPassword = aRawPassword("${password.value}-invalid")
        val accountCreatedEvent = anAccountCreatedEvent(password = password, status = AccountStatus.Active)
          .also { eventStore.appendToStream(it, AccountEvent::class) }

        val command = aLogInAccountCommand(email = accountCreatedEvent.emailAddress, password = invalidPassword)

        //act
        val result = handler.handle(command)

        //assert
        result.shouldBeLeft(nonEmptyListOf(LogInAccountError.InvalidPassword))
      }

      it("should fail when account is not active") {
        //arrange
        val password = aRawPassword()
        val accountCreatedEvent = anAccountCreatedEvent(password = password, status = AccountStatus.Staged)
          .also { eventStore.appendToStream(it, AccountEvent::class) }

        val command = aLogInAccountCommand(email = accountCreatedEvent.emailAddress, password = password)

        //act
        val result = handler.handle(command)

        //assert
        result.shouldBeLeft(nonEmptyListOf(LogInAccountError.AccountNotActive))
      }

      it("should accumulate errors") {
        //arrange
        val password = aRawPassword()
        val invalidPassword = aRawPassword("${password.value}-invalid")
        val accountCreatedEvent = anAccountCreatedEvent(password = password, status = AccountStatus.Staged)
          .also { eventStore.appendToStream(it, AccountEvent::class) }

        val command = aLogInAccountCommand(email = accountCreatedEvent.emailAddress, password = invalidPassword)

        //act
        val result = handler.handle(command)

        //assert
        result.shouldBeLeft(nonEmptyListOf(LogInAccountError.InvalidPassword, LogInAccountError.AccountNotActive))
      }

      it("should log it") {
        //arrange
        val password = aRawPassword()
        val accountCreatedEvent = anAccountCreatedEvent(password = password, status = AccountStatus.Active)
          .also { eventStore.appendToStream(it, AccountEvent::class) }

        //act
        val result = handler.handle(aLogInAccountCommand(email = accountCreatedEvent.emailAddress, password = password))

        //assert
        result.shouldBeRight()
      }
    }

  }

}
