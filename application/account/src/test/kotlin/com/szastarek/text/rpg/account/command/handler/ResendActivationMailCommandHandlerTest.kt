package com.szastarek.text.rpg.account.command.handler

import com.szastarek.text.rpg.account.AccountStatus
import com.szastarek.text.rpg.account.activation.AccountActivationUrlProvider
import com.szastarek.text.rpg.account.adapter.event.store.AccountAggregateEventStoreRepository
import com.szastarek.text.rpg.account.command.ResendActivationMailCommand
import com.szastarek.text.rpg.account.command.ResendActivationMailError
import com.szastarek.text.rpg.account.config.AccountActivationProperties
import com.szastarek.text.rpg.account.config.ActivateAccountMailProperties
import com.szastarek.text.rpg.account.event.AccountEvent
import com.szastarek.text.rpg.account.support.anAccountCreatedEvent
import com.szastarek.text.rpg.account.support.toAccountContext
import com.szastarek.text.rpg.acl.CoroutineAccountContext
import com.szastarek.text.rpg.acl.Role
import com.szastarek.text.rpg.acl.Roles
import com.szastarek.text.rpg.acl.SuperUserRole
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
import com.szastarek.text.rpg.utils.FixedClock
import io.kotest.assertions.arrow.core.shouldBeLeft
import io.kotest.assertions.arrow.core.shouldBeRight
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.property.Exhaustive
import io.kotest.property.checkAll
import io.kotest.property.exhaustive.enum
import io.kotest.property.exhaustive.filter
import io.ktor.http.Url
import kotlinx.coroutines.withContext
import kotlin.coroutines.coroutineContext
import kotlin.time.Duration.Companion.milliseconds

class ResendActivationMailCommandHandlerTest : DescribeSpec() {
	private val clock = FixedClock()
	private val mailSender = RecordingMailSender()
	private val mailProperties =
		ActivateAccountMailProperties(
			MailTemplateId("test-template"),
			anEmail("test-sender@mail.com"),
			MailSubject("test-subject"),
		)
	private val accountActivationProperties =
		AccountActivationProperties(
			activateAccountUrl = Url("http://test-host:3000/account/activate"),
			jwtConfig =
				JwtProperties(
					JwtSecret("activate-account-jwt-test-secret"),
					JwtIssuer("activate-account-jwt-test-issuer"),
					3600000.milliseconds,
				),
		)
	private val accountActivationUrlProvider = AccountActivationUrlProvider(accountActivationProperties, clock)
	private val eventStore = InMemoryEventStore()
	private val accountAggregateRepository = AccountAggregateEventStoreRepository(eventStore)
	private val acl = DefaultAuthorizedAccountAbilityProvider(CoroutineAccountContextProvider())

	private val handler =
		ResendActivationMailCommandHandler(
			accountAggregateRepository,
			mailSender,
			mailProperties,
			accountActivationUrlProvider,
			acl,
		)

	init {

		describe("ResendActivationMailCommandHandlerTest") {

			beforeTest {
				eventStore.clear()
				mailSender.clear()
			}

			it("should resend activation mail") {
				// arrange
				val stagedAccount =
					anAccountCreatedEvent(status = AccountStatus.Staged)
						.also { eventStore.appendToStream(it, AccountEvent::class) }
				val command = ResendActivationMailCommand(stagedAccount.emailAddress)

				// act
				val result =
					executeAs(SuperUserRole) {
						handler.handle(command)
					}

				// assert
				result.shouldBeRight()
				mailSender.hasBeenSent { it.to == stagedAccount.emailAddress && it.subject == mailProperties.subject }
			}

			Exhaustive.enum<AccountStatus>().filter { it != AccountStatus.Staged }.checkAll { status ->
				it("should return left when account is in status $status") {
					// arrange
					val account =
						anAccountCreatedEvent(status = status)
							.also { eventStore.appendToStream(it, AccountEvent::class) }
					val command = ResendActivationMailCommand(account.emailAddress)

					// act
					val result =
						executeAs(SuperUserRole) {
							handler.handle(command)
						}

					// assert
					result.shouldBeLeft(listOf(ResendActivationMailError.InvalidAccountStatus))
				}
			}

			it("should return left when account not found") {
				// arrange
				val notExistingEmail = anEmail()
				val command = ResendActivationMailCommand(notExistingEmail)

				// act
				val result =
					executeAs(SuperUserRole) {
						handler.handle(command)
					}

				// assert
				result.shouldBeLeft(listOf(ResendActivationMailError.AccountNotFound))
			}

			it("should throw exception when has insufficient permissions") {
				// arrange
				val stagedAccount =
					anAccountCreatedEvent(status = AccountStatus.Staged)
						.also { eventStore.appendToStream(it, AccountEvent::class) }
				val command = ResendActivationMailCommand(stagedAccount.emailAddress)

				// act & assert
				shouldThrow<AuthorityCheckException> {
					executeAs(Roles.RegularUser.role) {
						handler.handle(command)
					}
				}
			}
		}
	}

	private suspend fun <T> executeAs(
		role: Role,
		block: suspend () -> T,
	): T {
		val accountCreatedEvent = anAccountCreatedEvent(role = role)
		return withContext(coroutineContext + CoroutineAccountContext(accountCreatedEvent.toAccountContext())) {
			block()
		}
	}
}
