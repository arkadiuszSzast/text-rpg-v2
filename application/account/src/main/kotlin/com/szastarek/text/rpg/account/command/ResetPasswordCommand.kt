package com.szastarek.text.rpg.account.command

import arrow.core.Either
import arrow.core.Nel
import arrow.core.nel
import arrow.core.raise.either
import arrow.core.raise.ensure
import arrow.core.raise.zipOrAccumulate
import com.auth0.jwt.JWT
import com.szastarek.text.rpg.security.JwtToken
import com.szastarek.text.rpg.shared.password.RawPassword
import com.szastarek.text.rpg.shared.validate.ValidationError
import com.trendyol.kediatr.CommandWithResult

typealias ResetPasswordCommandResult = Either<Nel<ResetPasswordError>, ResetPasswordCommandSuccessResult>

data class ResetPasswordCommand(
  val token: JwtToken,
  val newPassword: RawPassword
) : CommandWithResult<ResetPasswordCommandResult> {
  companion object {
    operator fun invoke(token: String, newPassword: String) = either<Nel<ValidationError>, ResetPasswordCommand> {
      zipOrAccumulate(
        { e1, e2 -> e1 + e2 },
        {
          ensure(runCatching { JWT.decode(token) }.isSuccess) {
            ValidationError(".token", "validation.invalid_reset_password_token").nel()
          }
        },
        { RawPassword(newPassword).bind() },
        { _, pass -> ResetPasswordCommand(JwtToken(token), pass) }
      )
    }
  }
}

data object ResetPasswordCommandSuccessResult

enum class ResetPasswordError {
  AccountNotFound,
  InvalidSubject,
  InvalidToken
}
