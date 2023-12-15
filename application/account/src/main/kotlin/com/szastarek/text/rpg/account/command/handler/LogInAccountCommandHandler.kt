package com.szastarek.text.rpg.account.command.handler

import arrow.core.left
import arrow.core.nel
import com.szastarek.text.rpg.account.AccountAggregateRepository
import com.szastarek.text.rpg.account.RefreshToken
import com.szastarek.text.rpg.account.RefreshTokenRepository
import com.szastarek.text.rpg.account.command.LogInAccountCommand
import com.szastarek.text.rpg.account.command.LogInAccountCommandResult
import com.szastarek.text.rpg.account.command.LogInAccountCommandSuccessResult
import com.szastarek.text.rpg.account.command.LogInAccountError
import com.szastarek.text.rpg.security.AuthTokenProvider
import com.trendyol.kediatr.CommandWithResultHandler

class LogInAccountCommandHandler(
	private val accountAggregateRepository: AccountAggregateRepository,
	private val authTokenProvider: AuthTokenProvider,
	private val refreshTokenRedisRepository: RefreshTokenRepository,
) : CommandWithResultHandler<LogInAccountCommand, LogInAccountCommandResult> {
	override suspend fun handle(command: LogInAccountCommand): LogInAccountCommandResult {
		val account = accountAggregateRepository.findByEmail(command.emailAddress).getOrNull()
		if (account == null) {
			return LogInAccountError.AccountNotFound.nel().left()
		}

		val loginResult = account.logIn(command.password)

		return loginResult.map {
			val authToken =
				authTokenProvider.createAuthToken(
					account.accountId,
					account.emailAddress,
					account.role,
					account.customAuthorities,
				)
			val refreshToken = refreshTokenRedisRepository.replace(account.emailAddress, RefreshToken.generate())
			LogInAccountCommandSuccessResult(authToken, refreshToken)
		}
	}
}
