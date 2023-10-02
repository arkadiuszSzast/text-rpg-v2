package com.szastarek.text.rpg.account.command.handler

import arrow.core.Either
import arrow.core.raise.either
import arrow.core.toEitherNel
import com.auth0.jwt.JWT
import com.szastarek.text.rpg.account.AccountAggregateRepository
import com.szastarek.text.rpg.account.command.ResetPasswordCommand
import com.szastarek.text.rpg.account.command.ResetPasswordCommandResult
import com.szastarek.text.rpg.account.command.ResetPasswordCommandSuccessResult
import com.szastarek.text.rpg.account.command.ResetPasswordError
import com.szastarek.text.rpg.account.config.AccountResetPasswordProperties
import com.szastarek.text.rpg.account.event.AccountEvent
import com.szastarek.text.rpg.event.store.EventStoreWriteClient
import com.szastarek.text.rpg.event.store.appendToStream
import com.szastarek.text.rpg.event.store.revision
import com.szastarek.text.rpg.shared.email.EmailAddress
import com.trendyol.kediatr.CommandWithResultHandler

class ResetPasswordCommandHandler(
  private val accountAggregateRepository: AccountAggregateRepository,
  private val resetPasswordProperties: AccountResetPasswordProperties,
  private val eventStoreWriteClient: EventStoreWriteClient
) : CommandWithResultHandler<ResetPasswordCommand, ResetPasswordCommandResult>{
  override suspend fun handle(command: ResetPasswordCommand): ResetPasswordCommandResult = either {
    val (token, newPassword) = command
    val issuer = resetPasswordProperties.jwtIssuer
    val decodedJwt = Either.catch { JWT.decode(token.value) }.mapLeft { ResetPasswordError.InvalidToken }
      .toEitherNel().bind()
    val email = EmailAddress(decodedJwt.subject).mapLeft { ResetPasswordError.InvalidSubject }
      .toEitherNel().bind()
    val account = accountAggregateRepository.findByEmail(email).toEither { ResetPasswordError.AccountNotFound }
      .toEitherNel().bind()

    val event = account.resetPassword(token, issuer, newPassword).toEitherNel().bind()

    eventStoreWriteClient.appendToStream<AccountEvent>(event, event.revision())

    ResetPasswordCommandSuccessResult
  }
}