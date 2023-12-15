package com.szastarek.text.rpg.account.command.handler

import com.szastarek.text.rpg.account.adapter.event.store.AccountAggregateEventStoreRepository
import com.szastarek.text.rpg.account.command.SendResetPasswordCommand
import com.szastarek.text.rpg.account.command.SendResetPasswordError
import com.szastarek.text.rpg.account.config.AccountResetPasswordProperties
import com.szastarek.text.rpg.account.config.ResetPasswordMailProperties
import com.szastarek.text.rpg.account.event.AccountEvent
import com.szastarek.text.rpg.account.support.anAccountCreatedEvent
import com.szastarek.text.rpg.event.store.InMemoryEventStore
import com.szastarek.text.rpg.mail.MailSubject
import com.szastarek.text.rpg.mail.MailTemplateId
import com.szastarek.text.rpg.mail.RecordingMailSender
import com.szastarek.text.rpg.security.JwtIssuer
import com.szastarek.text.rpg.shared.anEmail
import com.szastarek.text.rpg.shared.email.EmailAddress
import com.szastarek.text.rpg.shared.validate.getOrThrow
import com.szastarek.text.rpg.utils.FixedClock
import io.kotest.assertions.arrow.core.shouldBeLeft
import io.kotest.assertions.arrow.core.shouldBeRight
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.collections.shouldBeEmpty
import io.ktor.http.Url
import kotlin.time.Duration.Companion.minutes

class SendResetPasswordCommandHandlerTest : DescribeSpec() {
	private val clock = FixedClock()
	private val eventStore = InMemoryEventStore()
	private val accountAggregateRepository = AccountAggregateEventStoreRepository(eventStore)
	private val resetPasswordProperties =
		AccountResetPasswordProperties(
			Url("http://test-host:3000/account/reset/password"),
			JwtIssuer("test-issuer"),
			15.minutes,
		)
	private val mailProperties =
		ResetPasswordMailProperties(
			MailTemplateId("reset-password-template-id"),
			EmailAddress("test-reset-password@mail.com").getOrThrow(),
			MailSubject("test-reset-password-subject"),
		)
	private val mailSender = RecordingMailSender()
	private val handler =
		SendResetPasswordCommandHandler(
			accountAggregateRepository,
			resetPasswordProperties,
			mailProperties,
			mailSender,
			clock,
		)

	init {

		describe("SendResetPasswordCommandHandlerTest") {

			beforeTest {
				mailSender.clear()
				eventStore.clear()
			}

			it("should sent reset password mail") {
				// arrange
				val accountCreatedEvent = anAccountCreatedEvent().also { eventStore.appendToStream(it, AccountEvent::class) }
				val command = SendResetPasswordCommand(accountCreatedEvent.emailAddress)

				// act
				val result = handler.handle(command)

				// assert
				result.shouldBeRight()
				mailSender.hasBeenSent {
					it.to == accountCreatedEvent.emailAddress && it.templateId == mailProperties.templateId
				}.shouldBeTrue()
			}

			it("should not send reset password mail when account not found") {
				// arrange
				val command = SendResetPasswordCommand(anEmail())

				// act
				val result = handler.handle(command)

				// assert
				result.shouldBeLeft(listOf(SendResetPasswordError.AccountNotFound))
				mailSender.getAll().shouldBeEmpty()
			}
		}
	}
}
