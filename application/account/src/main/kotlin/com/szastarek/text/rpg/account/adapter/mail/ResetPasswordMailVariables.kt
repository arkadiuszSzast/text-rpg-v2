package com.szastarek.text.rpg.account.adapter.mail

import com.szastarek.text.rpg.mail.MailVariables
import io.ktor.http.Url

data class ResetPasswordMailVariables(val resetPasswordUrl: Url) {
	companion object Keys {
		const val RESET_PASSWORD_URL = "reset_password_url"
	}

	fun toMailVariables() = MailVariables(mapOf(RESET_PASSWORD_URL to resetPasswordUrl.toString()))
}
