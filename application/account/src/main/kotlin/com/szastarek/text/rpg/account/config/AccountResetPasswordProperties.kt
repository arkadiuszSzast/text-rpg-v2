package com.szastarek.text.rpg.account.config

import com.szastarek.text.rpg.security.JwtIssuer
import io.ktor.http.Url
import kotlin.time.Duration

data class AccountResetPasswordProperties(
  val accountResetPasswordUrl: Url,
  val jwtIssuer: JwtIssuer,
  val jwtExpiration: Duration
)
