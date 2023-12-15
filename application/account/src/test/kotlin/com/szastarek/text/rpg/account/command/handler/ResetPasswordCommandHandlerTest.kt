package com.szastarek.text.rpg.account.command.handler

import arrow.core.nel
import com.szastarek.text.rpg.account.AccountAggregateBuilder
import com.szastarek.text.rpg.account.adapter.event.store.AccountAggregateEventStoreRepository
import com.szastarek.text.rpg.account.command.ResetPasswordCommand
import com.szastarek.text.rpg.account.command.ResetPasswordError
import com.szastarek.text.rpg.account.config.AccountResetPasswordProperties
import com.szastarek.text.rpg.account.event.AccountCreatedEvent
import com.szastarek.text.rpg.account.event.AccountEvent
import com.szastarek.text.rpg.account.support.anAccountCreatedEvent
import com.szastarek.text.rpg.event.store.InMemoryEventStore
import com.szastarek.text.rpg.security.JwtIssuer
import com.szastarek.text.rpg.security.JwtToken
import com.szastarek.text.rpg.shared.aRawPassword
import com.szastarek.text.rpg.shared.validate.getOrThrow
import com.szastarek.text.rpg.utils.FixedClock
import io.kotest.assertions.arrow.core.shouldBeLeft
import io.kotest.assertions.arrow.core.shouldBeRight
import io.kotest.core.spec.style.DescribeSpec
import io.ktor.http.Url
import kotlinx.datetime.Clock
import kotlin.time.Duration.Companion.minutes

class ResetPasswordCommandHandlerTest : DescribeSpec() {
	private val clock = FixedClock(Clock.System.now())
	private val eventStore = InMemoryEventStore()
	private val accountAggregateRepository = AccountAggregateEventStoreRepository(eventStore)
	private val resetPasswordProperties =
		AccountResetPasswordProperties(
			Url("http://test-host:3000/account/reset/password"),
			JwtIssuer("test-issuer"),
			15.minutes,
		)
	private val handler = ResetPasswordCommandHandler(accountAggregateRepository, resetPasswordProperties, eventStore)

	init {

		describe("ResetPasswordCommandHandlerTest") {

			it("should reset password") {
				// arrange
				val accountCreatedEvent = anAccountCreatedEvent().also { eventStore.appendToStream(it, AccountEvent::class) }
				val token = accountCreatedEvent.getResetPasswordToken()
				val newPassword = aRawPassword()

				val command = ResetPasswordCommand(token, newPassword)

				// act
				val result = handler.handle(command)

				// assert
				result.shouldBeRight()
			}
		}

		it("should not reset password when token is invalid") {
			// arrange
			val token = JwtToken("invalid-token")
			val newPassword = aRawPassword()

			val command = ResetPasswordCommand(token, newPassword)

			// act
			val result = handler.handle(command)

			// assert
			result.shouldBeLeft(listOf(ResetPasswordError.InvalidToken))
		}

		it("should not reset password when account not found") {
			// arrange
			val notSavedAccountCreatedEvent = anAccountCreatedEvent()
			val newPassword = aRawPassword()
			val command = ResetPasswordCommand(notSavedAccountCreatedEvent.getResetPasswordToken(), newPassword)

			// act
			val result = handler.handle(command)

			// assert
			result.shouldBeLeft(listOf(ResetPasswordError.AccountNotFound))
		}
	}

	private fun AccountCreatedEvent.getResetPasswordToken(): JwtToken {
		val account = AccountAggregateBuilder().apply(this.nel()).getOrThrow()
		return account.getResetPasswordToken(resetPasswordProperties.jwtIssuer, resetPasswordProperties.jwtExpiration, clock)
	}
}
