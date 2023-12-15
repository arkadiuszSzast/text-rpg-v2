package com.szastarek.text.rpg.account.command.handler

import com.szastarek.text.rpg.account.Account
import com.szastarek.text.rpg.account.AccountStatus
import com.szastarek.text.rpg.account.adapter.event.store.AccountAggregateEventStoreRepository
import com.szastarek.text.rpg.account.command.CreateRegularAccountCommand
import com.szastarek.text.rpg.account.event.AccountCreatedEvent
import com.szastarek.text.rpg.account.support.aCreateRegularAccountCommand
import com.szastarek.text.rpg.acl.Roles
import com.szastarek.text.rpg.event.store.InMemoryEventStore
import com.szastarek.text.rpg.utils.FixedClock
import io.kotest.assertions.arrow.core.shouldBeLeft
import io.kotest.assertions.arrow.core.shouldBeRight
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.should
import io.kotest.matchers.shouldBe
import org.litote.kmongo.Id

class CreateRegularAccountCommandHandlerTest : DescribeSpec() {
	private val clock = FixedClock()
	private val eventStore = InMemoryEventStore()
	private val accountAggregateRepository = AccountAggregateEventStoreRepository(eventStore)
	private val handler = CreateRegularAccountCommandHandler(accountAggregateRepository, eventStore, clock)

	init {

		describe("CreateRegularAccountCommandHandlerTest") {

			beforeTest { eventStore.clear() }

			it("should create account when email is not taken") {
				// arrange
				val command = aCreateRegularAccountCommand()

				// act
				val result = handler.handle(command)

				// assert
				val appendedEvent = eventStore.readStreamByEventType(AccountCreatedEvent.eventType, AccountCreatedEvent::class).single()
				result.shouldBeRight().should {
					val expectedEvent = command.toExpectedEvent(it.accountId)
					appendedEvent shouldBe expectedEvent
				}
			}

			it("should not create new account when email is taken") {
				// arrange
				val command = aCreateRegularAccountCommand()
				handler.handle(command)

				val takenEmailCommand = aCreateRegularAccountCommand(email = command.email)

				// act
				val result = handler.handle(takenEmailCommand)

				// assert
				result.shouldBeLeft()
			}
		}
	}

	private fun CreateRegularAccountCommand.toExpectedEvent(id: Id<Account>) =
		AccountCreatedEvent(
			id,
			email,
			AccountStatus.Staged,
			Roles.RegularUser.role,
			emptyList(),
			password,
			clock.now(),
			timeZoneId,
		)
}
