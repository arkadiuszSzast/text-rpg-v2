package com.szastarek.text.rpg.account.command.handler

import arrow.core.raise.either
import arrow.core.toEitherNel
import com.szastarek.text.rpg.account.AccountAggregateRepository
import com.szastarek.text.rpg.account.activation.AccountActivationTokenVerifier
import com.szastarek.text.rpg.account.command.ActivateAccountCommand
import com.szastarek.text.rpg.account.command.ActivateAccountCommandResult
import com.szastarek.text.rpg.account.command.ActivateAccountCommandSuccessResult
import com.szastarek.text.rpg.account.command.ActivateAccountError
import com.szastarek.text.rpg.account.event.AccountEvent
import com.szastarek.text.rpg.event.store.EventStoreWriteClient
import com.szastarek.text.rpg.event.store.appendToStream
import com.szastarek.text.rpg.event.store.revision
import com.szastarek.text.rpg.shared.email.EmailAddress
import com.trendyol.kediatr.CommandWithResultHandler

class ActivateAccountCommandHandler(
  private val accountAggregateRepository: AccountAggregateRepository,
  private val accountActivationTokenVerifier: AccountActivationTokenVerifier,
  private val eventStoreWriteClient: EventStoreWriteClient
) : CommandWithResultHandler<ActivateAccountCommand, ActivateAccountCommandResult> {

  override suspend fun handle(command: ActivateAccountCommand): ActivateAccountCommandResult = either {
      val token = command.token
      val validatedToken = accountActivationTokenVerifier.verify(token)

      val decodedToken = validatedToken.mapLeft { ActivateAccountError.InvalidJwt }.toEitherNel().bind().decodedJWT
      val subject = EmailAddress(decodedToken.subject).mapLeft { ActivateAccountError.InvalidSubject }.toEitherNel().bind()
      val account = accountAggregateRepository.findByEmail(subject).toEither { ActivateAccountError.AccountNotFound }.toEitherNel().bind()
      val event = account.activate().bind()

      eventStoreWriteClient.appendToStream<AccountEvent>(event, event.revision())
      ActivateAccountCommandSuccessResult(account.id)
    }
}