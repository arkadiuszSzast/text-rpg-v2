package com.szastarek.text.rpg.account.command.handler

import arrow.core.nel
import arrow.core.raise.either
import arrow.core.raise.ensure
import arrow.core.toEitherNel
import com.szastarek.text.rpg.account.AccountAggregate
import com.szastarek.text.rpg.account.AccountAggregateRepository
import com.szastarek.text.rpg.account.command.CreateWorldCreatorAccountCommand
import com.szastarek.text.rpg.account.command.CreateWorldCreatorAccountCommandResult
import com.szastarek.text.rpg.account.command.CreateWorldCreatorAccountCommandSuccessResult
import com.szastarek.text.rpg.account.command.CreateWorldCreatorAccountError
import com.szastarek.text.rpg.account.event.AccountEvent
import com.szastarek.text.rpg.account.world.creator.RegisterWorldCreatorTokenVerifier
import com.szastarek.text.rpg.acl.Roles
import com.szastarek.text.rpg.event.store.EventStoreWriteClient
import com.szastarek.text.rpg.event.store.appendToStream
import com.szastarek.text.rpg.event.store.revision
import com.trendyol.kediatr.CommandWithResultHandler
import kotlinx.datetime.Clock

class CreateWorldCreatorAccountCommandHandler(
	private val registerWorldCreatorTokenVerifier: RegisterWorldCreatorTokenVerifier,
	private val accountAggregateRepository: AccountAggregateRepository,
	private val eventStoreWriteClient: EventStoreWriteClient,
	private val clock: Clock,
) : CommandWithResultHandler<CreateWorldCreatorAccountCommand, CreateWorldCreatorAccountCommandResult> {
	override suspend fun handle(command: CreateWorldCreatorAccountCommand) =
		either {
			val token = command.token
			registerWorldCreatorTokenVerifier.verify(token, command.email).mapLeft { CreateWorldCreatorAccountError.InvalidToken }
				.toEitherNel().bind()
			ensure(accountAggregateRepository.findByEmail(command.email).isNone()) {
				CreateWorldCreatorAccountError.EmailAlreadyTaken.nel()
			}

			val event = AccountAggregate.create(command.email, Roles.WorldCreator.role, command.password, command.timeZoneId, clock)

			eventStoreWriteClient.appendToStream<AccountEvent>(event, event.revision())

			CreateWorldCreatorAccountCommandSuccessResult(event.accountId)
		}
}
