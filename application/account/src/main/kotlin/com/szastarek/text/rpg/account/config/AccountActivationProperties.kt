package com.szastarek.text.rpg.account.config

import com.szastarek.text.rpg.security.JwtProperties
import io.ktor.http.Url

data class AccountActivationProperties(val activateAccountUrl: Url, val jwtConfig: JwtProperties)
