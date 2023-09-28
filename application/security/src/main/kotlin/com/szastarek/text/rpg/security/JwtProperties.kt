package com.szastarek.text.rpg.security

import kotlin.time.Duration

data class JwtProperties(val secret: JwtSecret, val issuer: JwtIssuer, val expiration: Duration)

@JvmInline
value class JwtSecret(val value: String)

@JvmInline
value class JwtIssuer(val value: String)
