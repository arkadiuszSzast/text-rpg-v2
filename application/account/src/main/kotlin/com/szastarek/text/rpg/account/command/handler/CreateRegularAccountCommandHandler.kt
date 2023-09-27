package com.szastarek.text.rpg.account.command.handler

import arrow.core.left
import arrow.core.nel
import arrow.core.right
import com.szastarek.text.rpg.account.AccountAggregate
import com.szastarek.text.rpg.account.AccountAggregateRepository
import com.szastarek.text.rpg.account.command.CreateRegularAccountCommand
import com.szastarek.text.rpg.account.command.CreateRegularAccountCommandResult
import com.szastarek.text.rpg.account.command.CreateRegularAccountCommandSuccessResult
import com.szastarek.text.rpg.account.command.CreateAccountError
import com.szastarek.text.rpg.account.event.AccountEvent
import com.szastarek.text.rpg.acl.Roles
import com.szastarek.text.rpg.event.store.EventStoreWriteClient
import com.szastarek.text.rpg.event.store.appendToStream
import com.szastarek.text.rpg.event.store.revision
import com.trendyol.kediatr.CommandWithResultHandler
import kotlinx.datetime.Clock

class CreateRegularAccountCommandHandler(
  private val accountAggregateRepository: AccountAggregateRepository,
  private val eventStoreWriteClient: EventStoreWriteClient,
  private val clock: Clock
) : CommandWithResultHandler<CreateRegularAccountCommand, CreateRegularAccountCommandResult> {
  override suspend fun handle(command: CreateRegularAccountCommand): CreateRegularAccountCommandResult {
    val accountAlreadyExists = accountAggregateRepository.findByEmail(command.email)
    if (accountAlreadyExists.isSome()) {
      return CreateAccountError.EmailAlreadyTaken.nel().left()
    }

    val event = AccountAggregate.create(command.email, Roles.RegularUser.role, command.password, command.timeZoneId, clock)

    eventStoreWriteClient.appendToStream<AccountEvent>(event, event.revision())

    return CreateRegularAccountCommandSuccessResult(event.accountId).right()
  }
}
