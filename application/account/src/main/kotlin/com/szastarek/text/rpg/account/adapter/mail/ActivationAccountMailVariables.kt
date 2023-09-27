package com.szastarek.text.rpg.account.adapter.mail

import com.szastarek.text.rpg.mail.MailVariables

data class ActivationAccountMailVariables(val activationUrl: String) {
  fun toMailVariables() = MailVariables(mapOf("activate_account_url" to activationUrl))
}
