package com.szastarek.text.rpg.account.command.handler

import com.szastarek.text.rpg.account.adapter.event.store.AccountAggregateEventStoreRepository
import com.szastarek.text.rpg.account.command.ChangePasswordCommand
import com.szastarek.text.rpg.account.command.ChangePasswordError
import com.szastarek.text.rpg.account.event.AccountEvent
import com.szastarek.text.rpg.account.support.anAccountCreatedEvent
import com.szastarek.text.rpg.account.support.toAccountContext
import com.szastarek.text.rpg.acl.CoroutineAccountContext
import com.szastarek.text.rpg.acl.authority.DefaultAuthorizedAccountAbilityProvider
import com.szastarek.text.rpg.event.store.InMemoryEventStore
import com.szastarek.text.rpg.security.CoroutineAccountContextProvider
import com.szastarek.text.rpg.shared.aRawPassword
import io.kotest.assertions.arrow.core.shouldBeLeft
import io.kotest.assertions.arrow.core.shouldBeRight
import io.kotest.core.spec.style.DescribeSpec
import kotlinx.coroutines.withContext

class ChangePasswordCommandHandlerTest : DescribeSpec() {

  private val eventStore = InMemoryEventStore()

  private val accountAggregateRepository = AccountAggregateEventStoreRepository(eventStore)

  private val acl = DefaultAuthorizedAccountAbilityProvider(CoroutineAccountContextProvider())

  private val handler = ChangePasswordCommandHandler(accountAggregateRepository, eventStore, acl)

  init {

    describe("ChangePasswordCommandHandlerTest") {

      it("should change password") {
        //arrange
        val currentPassword = aRawPassword()
        val newPassword = aRawPassword()
        val accountCreatedEvent = anAccountCreatedEvent(password = currentPassword)
          .also { eventStore.appendToStream(it, AccountEvent::class) }
        val command = ChangePasswordCommand(currentPassword, newPassword, accountCreatedEvent.toAccountContext())

        //act
        val result = withContext(coroutineContext + CoroutineAccountContext(accountCreatedEvent.toAccountContext())) {
          handler.handle(command)
        }

        //assert
        result.shouldBeRight()
      }

      it("should not change password when current one does not match") {
        //arrange
        val currentPassword = aRawPassword()
        val newPassword = aRawPassword()
        val accountCreatedEvent = anAccountCreatedEvent(password = currentPassword)
          .also { eventStore.appendToStream(it, AccountEvent::class) }
        val command = ChangePasswordCommand(aRawPassword(), newPassword, accountCreatedEvent.toAccountContext())

        //act
        val result = withContext(coroutineContext + CoroutineAccountContext(accountCreatedEvent.toAccountContext())) {
          handler.handle(command)
        }

        //assert
        result.shouldBeLeft(listOf(ChangePasswordError.InvalidCurrentPassword))
      }

    }

  }
}
