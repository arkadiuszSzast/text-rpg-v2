package com.szastarek.text.rpg.account.command.handler

import arrow.core.nel
import arrow.core.raise.either
import arrow.core.raise.ensure
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
  override suspend fun handle(command: CreateRegularAccountCommand): CreateRegularAccountCommandResult = either {
    ensure(accountAggregateRepository.findByEmail(command.email).isNone()) { CreateAccountError.EmailAlreadyTaken.nel() }

    val event = AccountAggregate.create(command.email, Roles.RegularUser.role, command.password, command.timeZoneId, clock)

    eventStoreWriteClient.appendToStream<AccountEvent>(event, event.revision())

    CreateRegularAccountCommandSuccessResult(event.accountId)
  }
}
