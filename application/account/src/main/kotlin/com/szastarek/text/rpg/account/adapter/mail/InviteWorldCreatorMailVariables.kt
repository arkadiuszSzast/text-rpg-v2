package com.szastarek.text.rpg.account.adapter.mail

import com.szastarek.text.rpg.mail.MailVariables
import io.ktor.http.Url

data class InviteWorldCreatorMailVariables(val registerUrl: Url) {

  companion object Keys {
    const val registerUrl = "world_creator_register_url"
  }

  fun toMailVariables() = MailVariables(mapOf(Keys.registerUrl to registerUrl.toString()))
}
