package com.szastarek.text.rpg.account.command.handler

import arrow.core.raise.either
import arrow.core.toEitherNel
import com.szastarek.text.rpg.account.AccountAggregateRepository
import com.szastarek.text.rpg.account.command.ChangePasswordCommand
import com.szastarek.text.rpg.account.command.ChangePasswordCommandResult
import com.szastarek.text.rpg.account.command.ChangePasswordCommandSuccessResult
import com.szastarek.text.rpg.account.command.ChangePasswordError
import com.szastarek.text.rpg.account.event.AccountEvent
import com.szastarek.text.rpg.acl.authority.AuthorizedAccountAbilityProvider
import com.szastarek.text.rpg.event.store.EventStoreWriteClient
import com.szastarek.text.rpg.event.store.appendToStream
import com.trendyol.kediatr.CommandWithResultHandler

class ChangePasswordCommandHandler(
	private val accountAggregateRepository: AccountAggregateRepository,
	private val eventStoreWriteClient: EventStoreWriteClient,
	private val acl: AuthorizedAccountAbilityProvider,
) : CommandWithResultHandler<ChangePasswordCommand, ChangePasswordCommandResult> {
	override suspend fun handle(command: ChangePasswordCommand): ChangePasswordCommandResult =
		either {
			val (currentPassword, newPassword, accountContext) = command
			val account =
				accountAggregateRepository.findByEmail(accountContext.email)
					.toEither { ChangePasswordError.AccountNotFound }.toEitherNel().bind()
			acl.ensuring().ensureCanUpdate(account)

			val event = account.changePassword(currentPassword, newPassword).toEitherNel().bind()
			eventStoreWriteClient.appendToStream<AccountEvent>(event)

			ChangePasswordCommandSuccessResult
		}
}
