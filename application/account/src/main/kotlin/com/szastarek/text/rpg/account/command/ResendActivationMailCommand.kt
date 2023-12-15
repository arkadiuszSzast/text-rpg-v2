package com.szastarek.text.rpg.account.command

import arrow.core.Either
import arrow.core.Nel
import arrow.core.raise.either
import com.szastarek.text.rpg.shared.email.EmailAddress
import com.trendyol.kediatr.CommandWithResult

typealias ResendActivationMailCommandResult = Either<Nel<ResendActivationMailError>, ResendActivationMailSuccessResult>

data class ResendActivationMailCommand(val email: EmailAddress) : CommandWithResult<ResendActivationMailCommandResult> {
	companion object {
		operator fun invoke(email: String) =
			either {
				val mail = EmailAddress(email).bind()
				ResendActivationMailCommand(mail)
			}
	}
}

data object ResendActivationMailSuccessResult

enum class ResendActivationMailError {
	AccountNotFound,
	InvalidAccountStatus,
}
