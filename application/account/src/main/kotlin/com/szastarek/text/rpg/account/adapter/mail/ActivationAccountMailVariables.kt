package com.szastarek.text.rpg.account.adapter.mail

import com.szastarek.text.rpg.mail.MailVariables
import io.ktor.http.Url

data class ActivationAccountMailVariables(val activationUrl: Url) {

  companion object Keys {
    const val activateAccountUrl = "activate_account_url"
  }

  fun toMailVariables() = MailVariables(mapOf(activateAccountUrl to activationUrl.toString()))
}
