package com.szastarek.text.rpg.account.adapter.mail

import com.szastarek.text.rpg.mail.MailVariables
import io.ktor.http.*

data class ActivationAccountMailVariables(val activationUrl: Url) {
  fun toMailVariables() = MailVariables(mapOf("activate_account_url" to activationUrl.toString()))
}
