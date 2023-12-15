package com.szastarek.text.rpg.account.command

import arrow.core.Either
import arrow.core.Nel
import arrow.core.raise.either
import com.szastarek.text.rpg.account.RefreshToken
import com.szastarek.text.rpg.security.JwtToken
import com.szastarek.text.rpg.shared.email.EmailAddress
import com.trendyol.kediatr.CommandWithResult

typealias RefreshAuthTokenCommandResult = Either<Nel<RefreshAuthTokenError>, RefreshAuthTokenCommandSuccessResult>

data class RefreshAuthTokenCommand(
	val accountEmail: EmailAddress,
	val refreshToken: RefreshToken,
) : CommandWithResult<RefreshAuthTokenCommandResult> {
	companion object {
		operator fun invoke(
			accountEmail: String,
			refreshToken: String,
		) = either {
			val mail = EmailAddress(accountEmail).bind()
			RefreshAuthTokenCommand(mail, RefreshToken(refreshToken))
		}
	}
}

data class RefreshAuthTokenCommandSuccessResult(val authToken: JwtToken, val refreshToken: RefreshToken)

enum class RefreshAuthTokenError {
	RefreshTokenNotFound,
	InvalidRefreshToken,
	AccountNotFound,
	AccountInInvalidStatus,
}
