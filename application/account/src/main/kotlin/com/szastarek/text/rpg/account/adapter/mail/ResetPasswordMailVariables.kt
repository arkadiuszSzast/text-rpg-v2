package com.szastarek.text.rpg.account.adapter.mail

import com.szastarek.text.rpg.mail.MailVariables
import io.ktor.http.*

data class ResetPasswordMailVariables(val resetPasswordUrl: Url) {

  companion object Keys {
    const val resetPasswordUrl = "reset_password_url"
  }

  fun toMailVariables() = MailVariables(mapOf(Keys.resetPasswordUrl to resetPasswordUrl.toString()))
}