package com.szastarek.text.rpg.account.command.handler

import com.szastarek.text.rpg.account.AccountStatus
import com.szastarek.text.rpg.account.RefreshToken
import com.szastarek.text.rpg.account.adapter.event.store.AccountAggregateEventStoreRepository
import com.szastarek.text.rpg.account.command.RefreshAuthTokenCommand
import com.szastarek.text.rpg.account.command.RefreshAuthTokenError
import com.szastarek.text.rpg.account.event.AccountEvent
import com.szastarek.text.rpg.account.support.InMemoryRefreshTokenRepository
import com.szastarek.text.rpg.account.support.anAccountCreatedEvent
import com.szastarek.text.rpg.event.store.InMemoryEventStore
import com.szastarek.text.rpg.security.AuthTokenProvider
import com.szastarek.text.rpg.security.config.AuthenticationProperties
import com.szastarek.text.rpg.utils.FixedClock
import io.kotest.assertions.arrow.core.shouldBeLeft
import io.kotest.assertions.arrow.core.shouldBeRight
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.property.Exhaustive
import io.kotest.property.checkAll
import io.kotest.property.exhaustive.enum
import io.kotest.property.exhaustive.filter
import kotlin.time.Duration.Companion.hours

class RefreshAuthTokenCommandHandlerTest : DescribeSpec() {

  private val clock = FixedClock()
  private val eventStore = InMemoryEventStore()
  private val accountAggregateRepository = AccountAggregateEventStoreRepository(eventStore)
  private val refreshTokenRepository = InMemoryRefreshTokenRepository()
  private val authenticationProperties = AuthenticationProperties(
    jwtAudience = "test-audience",
    jwtIssuer = "test-issuer",
    jwtRealm = "test-realm",
    jwtSecret = "test-secret",
    authTokenExpiration = 1.hours,
  )
  private val authTokenProvider = AuthTokenProvider(authenticationProperties, clock)
  private val handler = RefreshAuthTokenCommandHandler(refreshTokenRepository, accountAggregateRepository, authTokenProvider)

  init {

    describe("RefreshAuthTokenCommandHandlerTest") {

      beforeTest {
        eventStore.clear()
        refreshTokenRepository.clear()
      }

      it("should refresh token") {
        //arrange
        val account = anAccountCreatedEvent(status = AccountStatus.Active)
          .also { eventStore.appendToStream(it, AccountEvent::class) }
        val currentRefreshToken = refreshTokenRepository.replace(account.emailAddress, RefreshToken.generate())

        //act
        val result = handler.handle(RefreshAuthTokenCommand(account.emailAddress, currentRefreshToken))

        //assert
        result.shouldBeRight()
      }

      it("should not refresh when given token is invalid") {
        //arrange
        val account = anAccountCreatedEvent().also { eventStore.appendToStream(it, AccountEvent::class) }
        val currentRefreshToken = refreshTokenRepository.replace(account.emailAddress, RefreshToken.generate())
        val invalidRefreshToken = RefreshToken.generate()

        //act
        val result = handler.handle(RefreshAuthTokenCommand(account.emailAddress, invalidRefreshToken))

        //assert
        result.shouldBeLeft(listOf(RefreshAuthTokenError.InvalidRefreshToken))
      }

      it("should not refresh when refresh token is not persisted") {
        //arrange
        val account = anAccountCreatedEvent().also { eventStore.appendToStream(it, AccountEvent::class) }

        //act
        val result = handler.handle(RefreshAuthTokenCommand(account.emailAddress, RefreshToken.generate()))

        //assert
        result.shouldBeLeft(listOf(RefreshAuthTokenError.RefreshTokenNotFound))
      }

      it("should not refresh when account not found") {
        //arrange
        val notExistingAccount = anAccountCreatedEvent()
        val currentRefreshToken =
          refreshTokenRepository.replace(notExistingAccount.emailAddress, RefreshToken.generate())

        //act
        val result = handler.handle(RefreshAuthTokenCommand(notExistingAccount.emailAddress, currentRefreshToken))

        //assert
        result.shouldBeLeft(listOf(RefreshAuthTokenError.AccountNotFound))
      }

      Exhaustive.enum<AccountStatus>().filter { it != AccountStatus.Active }.checkAll { status ->
        it("should not refresh when account is in $status status") {
          //arrange
          val notExistingAccount =
            anAccountCreatedEvent(status = status).also { eventStore.appendToStream(it, AccountEvent::class) }
          val currentRefreshToken =
            refreshTokenRepository.replace(notExistingAccount.emailAddress, RefreshToken.generate())

          //act
          val result = handler.handle(RefreshAuthTokenCommand(notExistingAccount.emailAddress, currentRefreshToken))

          //assert
          result.shouldBeLeft(listOf(RefreshAuthTokenError.AccountInInvalidStatus))
        }
      }
    }
  }
}
