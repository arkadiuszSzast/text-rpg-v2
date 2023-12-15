package com.szastarek.text.rpg.account.command.handler

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.szastarek.text.rpg.account.AccountStatus
import com.szastarek.text.rpg.account.activation.AccountActivationTokenVerifier
import com.szastarek.text.rpg.account.activation.AccountActivationUrlProvider
import com.szastarek.text.rpg.account.adapter.event.store.AccountAggregateEventStoreRepository
import com.szastarek.text.rpg.account.command.ActivateAccountCommand
import com.szastarek.text.rpg.account.command.ActivateAccountCommandSuccessResult
import com.szastarek.text.rpg.account.command.ActivateAccountError
import com.szastarek.text.rpg.account.config.AccountActivationProperties
import com.szastarek.text.rpg.account.event.AccountActivatedEvent
import com.szastarek.text.rpg.account.event.AccountCreatedEvent
import com.szastarek.text.rpg.account.event.AccountEvent
import com.szastarek.text.rpg.account.support.anAccountCreatedEvent
import com.szastarek.text.rpg.event.store.InMemoryEventStore
import com.szastarek.text.rpg.security.JwtIssuer
import com.szastarek.text.rpg.security.JwtProperties
import com.szastarek.text.rpg.security.JwtSecret
import com.szastarek.text.rpg.shared.Version
import com.szastarek.text.rpg.shared.anEmail
import com.szastarek.text.rpg.shared.validate.getOrThrow
import com.szastarek.text.rpg.utils.FixedClock
import io.kotest.assertions.arrow.core.shouldBeLeft
import io.kotest.assertions.arrow.core.shouldBeRight
import io.kotest.assertions.arrow.core.shouldBeSome
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.should
import io.kotest.matchers.shouldBe
import io.ktor.http.Url
import kotlinx.datetime.Clock
import kotlinx.datetime.toJavaInstant
import kotlin.time.Duration.Companion.milliseconds

class ActivateAccountCommandHandlerTest : DescribeSpec() {

  private val clock = FixedClock(Clock.System.now())
  private val eventStore = InMemoryEventStore()
  private val accountAggregateRepository = AccountAggregateEventStoreRepository(eventStore)
  private val accountActivationProperties = AccountActivationProperties(
    activateAccountUrl = Url("http://test-host:3000/account/activate"),
    jwtConfig = JwtProperties(
      JwtSecret("activate-account-jwt-test-secret"),
      JwtIssuer("activate-account-jwt-test-issuer"),
      3600000.milliseconds
    )
  )
  private val accountActivationTokenVerifier = AccountActivationTokenVerifier(accountActivationProperties)
  private val accountActivationUrlProvider = AccountActivationUrlProvider(accountActivationProperties, clock)
  private val handler =
    ActivateAccountCommandHandler(accountAggregateRepository, accountActivationTokenVerifier, eventStore)

  init {

    describe("ActivateAccountCommandHandlerTest") {

      it("should activate account") {
        //arrange
        val accountCreatedEvent = anAccountCreatedEvent(status = AccountStatus.Staged)
          .also { eventStore.appendToStream(it, AccountEvent::class) }
        val token = accountActivationUrlProvider.provide(accountCreatedEvent.emailAddress).parameters["token"]!!
        val command = ActivateAccountCommand(token).getOrThrow()

        //act
        val result = handler.handle(command)

        //assert
        result.shouldBeRight(ActivateAccountCommandSuccessResult(accountCreatedEvent.accountId))
        accountAggregateRepository.findByEmail(accountCreatedEvent.emailAddress).shouldBeSome() should {
          it.status shouldBe AccountStatus.Active
          it.version shouldBe Version(1L)
        }
      }

      it("should return left when token is incorrect") {
        //arrange
        val token = JWT.create().sign(Algorithm.HMAC256("invalid-secret"))
        val command = ActivateAccountCommand(token).getOrThrow()

        //act
        val result = handler.handle(command)

        //assert
        result.shouldBeLeft(listOf(ActivateAccountError.InvalidJwt))
      }

      it("should return left when subject is not valid email") {
        //arrange
        val jwtConfig = accountActivationProperties.jwtConfig
        val token = JWT.create()
          .withIssuer(jwtConfig.issuer.value)
          .withSubject("invalid-email")
          .withExpiresAt(clock.now().plus(jwtConfig.expiration).toJavaInstant())
          .sign(Algorithm.HMAC256(jwtConfig.secret.value))
        val command = ActivateAccountCommand(token).getOrThrow()

        //act
        val result = handler.handle(command)

        //assert
        result.shouldBeLeft(listOf(ActivateAccountError.InvalidSubject))
      }

      it("should return left when account not found") {
        //arrange
        val token = accountActivationUrlProvider.provide(anEmail()).parameters["token"]!!
        val command = ActivateAccountCommand(token).getOrThrow()

        //act
        val result = handler.handle(command)

        //assert
        result.shouldBeLeft(listOf(ActivateAccountError.AccountNotFound))
      }

      it("should return left when account is already active") {
        //arrange
        val accountCreatedEvent = anAccountCreatedEvent(status = AccountStatus.Staged)
          .also {
            eventStore.appendToStream(it, AccountEvent::class)
            eventStore.appendToStream(it.accountActivated(), AccountEvent::class)
          }
        val token = accountActivationUrlProvider.provide(accountCreatedEvent.emailAddress).parameters["token"]!!
        val command = ActivateAccountCommand(token).getOrThrow()

        //act
        val result = handler.handle(command)

        //assert
        result.shouldBeLeft(listOf(ActivateAccountError.InvalidAccountStatus))
      }
    }
  }

  private fun AccountCreatedEvent.accountActivated() = AccountActivatedEvent(accountId, emailAddress, version.next())
}