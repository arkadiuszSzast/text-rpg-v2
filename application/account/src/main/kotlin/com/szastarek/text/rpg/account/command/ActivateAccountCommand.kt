package com.szastarek.text.rpg.account.command

import arrow.core.Either
import arrow.core.Nel
import arrow.core.nel
import arrow.core.raise.either
import arrow.core.raise.ensure
import com.auth0.jwt.JWT
import com.szastarek.text.rpg.account.Account
import com.szastarek.text.rpg.security.JwtToken
import com.szastarek.text.rpg.shared.validate.ValidationError
import com.trendyol.kediatr.CommandWithResult
import org.litote.kmongo.Id

typealias ActivateAccountCommandResult = Either<Nel<ActivateAccountError>, ActivateAccountCommandSuccessResult>

data class ActivateAccountCommand private constructor(val token: JwtToken): CommandWithResult<ActivateAccountCommandResult> {
  companion object {
    operator fun invoke(token: String) = either {
      ensure(runCatching { JWT.decode(token) }.isSuccess) {
        ValidationError(
          ".token",
          ".validation.invalid_account_activation_token"
        ).nel()
      }
      ActivateAccountCommand(JwtToken(token))
    }
  }
}

data class ActivateAccountCommandSuccessResult(val accountId: Id<Account>)

enum class ActivateAccountError {
  AccountNotFound,
  InvalidJwt,
  InvalidSubject,
  InvalidAccountStatus
}