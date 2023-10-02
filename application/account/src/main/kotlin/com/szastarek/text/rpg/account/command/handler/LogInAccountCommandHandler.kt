package com.szastarek.text.rpg.account.command.handler

import arrow.core.left
import arrow.core.nel
import com.szastarek.text.rpg.account.AccountAggregateRepository
import com.szastarek.text.rpg.account.command.LogInAccountCommand
import com.szastarek.text.rpg.account.command.LogInAccountCommandResult
import com.szastarek.text.rpg.account.command.LogInAccountCommandSuccessResult
import com.szastarek.text.rpg.account.command.LogInAccountError
import com.szastarek.text.rpg.account.toAccountId
import com.szastarek.text.rpg.security.AuthTokenProvider
import com.trendyol.kediatr.CommandWithResultHandler

class LogInAccountCommandHandler(
  private val accountAggregateRepository: AccountAggregateRepository,
  private val authTokenProvider: AuthTokenProvider
) : CommandWithResultHandler<LogInAccountCommand, LogInAccountCommandResult> {

  override suspend fun handle(command: LogInAccountCommand): LogInAccountCommandResult {
    val account = accountAggregateRepository.findByEmail(command.emailAddress).getOrNull()
    if (account == null) {
      return LogInAccountError.AccountNotFound.nel().left()
    }

    val loginResult = account.logIn(command.password)

    return loginResult.map {
      val token = authTokenProvider.createToken(
        account.id.toAccountId(),
        account.emailAddress,
        account.role,
        account.customAuthorities
      )
      LogInAccountCommandSuccessResult(token)
    }
  }
}