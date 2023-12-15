package com.szastarek.text.rpg.account.command.handler

import arrow.core.nel
import arrow.core.raise.either
import arrow.core.raise.ensure
import arrow.core.raise.ensureNotNull
import com.szastarek.text.rpg.account.AccountAggregateRepository
import com.szastarek.text.rpg.account.AccountStatus
import com.szastarek.text.rpg.account.RefreshToken
import com.szastarek.text.rpg.account.RefreshTokenRepository
import com.szastarek.text.rpg.account.command.RefreshAuthTokenCommand
import com.szastarek.text.rpg.account.command.RefreshAuthTokenCommandResult
import com.szastarek.text.rpg.account.command.RefreshAuthTokenCommandSuccessResult
import com.szastarek.text.rpg.account.command.RefreshAuthTokenError
import com.szastarek.text.rpg.security.AuthTokenProvider
import com.trendyol.kediatr.CommandWithResultHandler

class RefreshAuthTokenCommandHandler(
	private val refreshTokenRepository: RefreshTokenRepository,
	private val accountAggregateRepository: AccountAggregateRepository,
	private val authTokenProvider: AuthTokenProvider,
) : CommandWithResultHandler<RefreshAuthTokenCommand, RefreshAuthTokenCommandResult> {
	override suspend fun handle(command: RefreshAuthTokenCommand): RefreshAuthTokenCommandResult =
		either {
			val (accountEmail, refreshToken) = command
			val foundRefreshToken =
				ensureNotNull(refreshTokenRepository.getAndDelete(accountEmail).getOrNull()) {
					RefreshAuthTokenError.RefreshTokenNotFound.nel()
				}
			ensure(foundRefreshToken == refreshToken) { RefreshAuthTokenError.InvalidRefreshToken.nel() }

			val accountAggregate =
				ensureNotNull(accountAggregateRepository.findByEmail(accountEmail).getOrNull()) {
					RefreshAuthTokenError.AccountNotFound.nel()
				}
			ensure(accountAggregate.status == AccountStatus.Active) { RefreshAuthTokenError.AccountInInvalidStatus.nel() }

			val newRefreshToken = refreshTokenRepository.replace(accountEmail, RefreshToken.generate())
			val newAuthToken =
				authTokenProvider.createAuthToken(
					accountAggregate.accountId,
					accountAggregate.emailAddress,
					accountAggregate.role,
					accountAggregate.customAuthorities,
				)
			RefreshAuthTokenCommandSuccessResult(newAuthToken, newRefreshToken)
		}
}
