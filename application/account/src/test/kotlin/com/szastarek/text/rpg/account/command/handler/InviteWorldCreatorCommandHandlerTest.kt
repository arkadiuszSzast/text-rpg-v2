package com.szastarek.text.rpg.account.command.handler

import com.szastarek.text.rpg.account.adapter.event.store.AccountAggregateEventStoreRepository
import com.szastarek.text.rpg.account.command.InviteWorldCreatorCommand
import com.szastarek.text.rpg.account.command.InviteWorldCreatorError
import com.szastarek.text.rpg.account.config.InviteWorldCreatorMailProperties
import com.szastarek.text.rpg.account.config.WorldCreatorRegisterProperties
import com.szastarek.text.rpg.account.event.AccountEvent
import com.szastarek.text.rpg.account.support.anAccountCreatedEvent
import com.szastarek.text.rpg.account.support.toAccountContext
import com.szastarek.text.rpg.account.world.creator.WorldCreatorRegisterUrlProvider
import com.szastarek.text.rpg.acl.CoroutineAccountContext
import com.szastarek.text.rpg.acl.Roles
import com.szastarek.text.rpg.acl.authority.AuthorityCheckException
import com.szastarek.text.rpg.acl.authority.DefaultAuthorizedAccountAbilityProvider
import com.szastarek.text.rpg.event.store.InMemoryEventStore
import com.szastarek.text.rpg.mail.MailSubject
import com.szastarek.text.rpg.mail.MailTemplateId
import com.szastarek.text.rpg.mail.RecordingMailSender
import com.szastarek.text.rpg.security.CoroutineAccountContextProvider
import com.szastarek.text.rpg.security.JwtIssuer
import com.szastarek.text.rpg.security.JwtProperties
import com.szastarek.text.rpg.security.JwtSecret
import com.szastarek.text.rpg.shared.anEmail
import com.szastarek.text.rpg.shared.email.EmailAddress
import com.szastarek.text.rpg.shared.validate.getOrThrow
import com.szastarek.text.rpg.utils.FixedClock
import io.kotest.assertions.arrow.core.shouldBeLeft
import io.kotest.assertions.arrow.core.shouldBeRight
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.ktor.http.Url
import kotlinx.coroutines.withContext
import kotlin.time.Duration.Companion.milliseconds

class InviteWorldCreatorCommandHandlerTest : DescribeSpec() {
	private val clock = FixedClock()
	private val eventStore = InMemoryEventStore()
	private val accountAggregateRepository = AccountAggregateEventStoreRepository(eventStore)
	private val worldCreatorRegisterProperties =
		WorldCreatorRegisterProperties(
			registerUrl = Url("http://test-host:3000/account/world-creator"),
			jwtConfig =
				JwtProperties(
					JwtSecret("world-creator-register-jwt-test-secret"),
					JwtIssuer("world-creator-register-jwt-test-issuer"),
					60000.milliseconds,
				),
		)
	private val worldCreatorRegisterUrlProvider =
		WorldCreatorRegisterUrlProvider(worldCreatorRegisterProperties, clock)
	private val inviteWorldCreatorMailProperties =
		InviteWorldCreatorMailProperties(
			templateId = MailTemplateId("invite-world-creator-test-templateId"),
			sender = EmailAddress("invite-world-creator-test-sender@mail.com").getOrThrow(),
			subject = MailSubject("invite-world-creator-test-subject"),
		)
	private val mailSender = RecordingMailSender()
	private val acl = DefaultAuthorizedAccountAbilityProvider(CoroutineAccountContextProvider())
	private val handler =
		InviteWorldCreatorCommandHandler(
			worldCreatorRegisterUrlProvider,
			inviteWorldCreatorMailProperties,
			accountAggregateRepository,
			mailSender,
			acl,
		)

	init {

		describe("InviteWorldCreatorCommandHandlerTest") {

			it("should invite world creator") {
				// arrange
				val superUserCreatedEvent = anAccountCreatedEvent(role = Roles.SuperUser.role)
				val command = InviteWorldCreatorCommand(anEmail())

				// act
				val result =
					withContext(coroutineContext + CoroutineAccountContext(superUserCreatedEvent.toAccountContext())) {
						handler.handle(command)
					}

				// assert
				result.shouldBeRight()
			}

			it("regular user cannot invite world creator") {
				// arrange
				val userCreatedEvent = anAccountCreatedEvent(role = Roles.RegularUser.role)
				val command = InviteWorldCreatorCommand(anEmail())

				// act & assert
				withContext(coroutineContext + CoroutineAccountContext(userCreatedEvent.toAccountContext())) {
					shouldThrow<AuthorityCheckException> {
						handler.handle(command)
					}
				}
			}

			it("should not invite world creator when email is already taken") {
				// arrange
				val superUserCreatedEvent = anAccountCreatedEvent(role = Roles.SuperUser.role)
				val existingAccount = anAccountCreatedEvent().also { eventStore.appendToStream(it, AccountEvent::class) }
				val command = InviteWorldCreatorCommand(existingAccount.emailAddress)

				// act
				val result =
					withContext(coroutineContext + CoroutineAccountContext(superUserCreatedEvent.toAccountContext())) {
						handler.handle(command)
					}

				// assert
				result.shouldBeLeft(listOf(InviteWorldCreatorError.EmailAlreadyTaken))
			}
		}
	}
}
