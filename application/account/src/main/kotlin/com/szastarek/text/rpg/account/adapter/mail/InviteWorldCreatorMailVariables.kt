package com.szastarek.text.rpg.account.adapter.mail

import com.szastarek.text.rpg.mail.MailVariables
import io.ktor.http.Url

data class InviteWorldCreatorMailVariables(val registerUrl: Url) {
	companion object Keys {
		const val REGISTER_URL = "world_creator_register_url"
	}

	fun toMailVariables() = MailVariables(mapOf(REGISTER_URL to registerUrl.toString()))
}
