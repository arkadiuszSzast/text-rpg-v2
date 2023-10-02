package com.szastarek.text.rpg.account.command

import arrow.core.Either
import arrow.core.Nel
import arrow.core.raise.either
import com.szastarek.text.rpg.shared.email.EmailAddress
import com.trendyol.kediatr.CommandWithResult

typealias SendResetPasswordCommandResult = Either<Nel<SendResetPasswordError>, SendResetPasswordSuccessResult>

data class SendResetPasswordCommand(val emailAddress: EmailAddress) :
  CommandWithResult<SendResetPasswordCommandResult> {
  companion object {
    operator fun invoke(email: String) = either {
      val emailAddress = EmailAddress(email).bind()
      SendResetPasswordCommand(emailAddress)
    }
  }
}

data object SendResetPasswordSuccessResult

enum class SendResetPasswordError {
  AccountNotFound
}

