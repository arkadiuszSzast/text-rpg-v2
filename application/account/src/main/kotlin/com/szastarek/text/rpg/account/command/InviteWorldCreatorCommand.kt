package com.szastarek.text.rpg.account.command

import arrow.core.Either
import arrow.core.Nel
import arrow.core.raise.either
import com.szastarek.text.rpg.shared.email.EmailAddress
import com.trendyol.kediatr.CommandWithResult

typealias InviteWorldCreatorCommandResult = Either<Nel<InviteWorldCreatorError>, InviteWorldCreatorCommandSuccessResult>

data class InviteWorldCreatorCommand(
  val email: EmailAddress
) : CommandWithResult<InviteWorldCreatorCommandResult> {
  companion object {
    operator fun invoke(email: String) = either {
      val mail = EmailAddress(email).bind()
      InviteWorldCreatorCommand(mail)
    }
  }
}

data object InviteWorldCreatorCommandSuccessResult

enum class InviteWorldCreatorError {
  EmailAlreadyTaken
}