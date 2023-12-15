package com.szastarek.text.rpg.account.command

import arrow.core.Either
import arrow.core.Nel
import arrow.core.raise.either
import com.szastarek.text.rpg.acl.AuthenticatedAccountContext
import com.szastarek.text.rpg.shared.password.RawPassword
import com.trendyol.kediatr.CommandWithResult

typealias ChangePasswordCommandResult = Either<Nel<ChangePasswordError>, ChangePasswordCommandSuccessResult>

data class ChangePasswordCommand(
	val currentPassword: RawPassword,
	val newPassword: RawPassword,
	val authenticatedAccountContext: AuthenticatedAccountContext,
) : CommandWithResult<ChangePasswordCommandResult> {
	companion object {
		operator fun invoke(
			currentPassword: String,
			newPassword: String,
			authenticatedAccountContext: AuthenticatedAccountContext,
		) = either {
			val current = RawPassword.createWithoutValidation(currentPassword)
			val new = RawPassword(newPassword).bind()

			ChangePasswordCommand(current, new, authenticatedAccountContext)
		}
	}
}

data object ChangePasswordCommandSuccessResult

enum class ChangePasswordError {
	AccountNotFound,
	InvalidCurrentPassword,
}
