package com.szastarek.text.rpg.security.config

data class AuthenticationProperties(
    val jwtAudience: String,
    val jwtIssuer:String,
    val jwtRealm: String,
    val jwtSecret: String,
    val expirationInMillis: Long,
)
