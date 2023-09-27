package com.szastarek.text.rpg.account.command

import arrow.core.Either
import arrow.core.Nel
import arrow.core.raise.either
import arrow.core.raise.ensure
import com.szastarek.text.rpg.security.JwtToken
import com.szastarek.text.rpg.shared.MaskedString
import com.szastarek.text.rpg.shared.email.EmailAddress
import com.szastarek.text.rpg.shared.password.RawPassword
import com.szastarek.text.rpg.shared.validate.ValidationErrors
import com.trendyol.kediatr.CommandWithResult

typealias LogInAccountCommandResult = Either<Nel<LogInAccountError>, LogInAccountCommandSuccessResult>

data class LogInAccountCommand(val emailAddress: EmailAddress, val password: RawPassword): CommandWithResult<LogInAccountCommandResult> {
  companion object {
    operator fun invoke(email: String, password: MaskedString) = either<ValidationErrors, LogInAccountCommand> {
      val rawPassword = RawPassword.createWithoutValidation(password.value)
      val emailAddress = EmailAddress(email).bind()
      LogInAccountCommand(emailAddress, rawPassword)
    }
  }
}

data class LogInAccountCommandSuccessResult(val token: JwtToken)

enum class LogInAccountError {
  InvalidPassword,
  AccountNotFound,
  AccountNotActive
}
