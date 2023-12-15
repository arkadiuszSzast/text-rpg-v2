package com.szastarek.text.rpg.security.config

import kotlin.time.Duration

data class AuthenticationProperties(
	val jwtAudience: String,
	val jwtIssuer: String,
	val jwtRealm: String,
	val jwtSecret: String,
	val authTokenExpiration: Duration,
)
