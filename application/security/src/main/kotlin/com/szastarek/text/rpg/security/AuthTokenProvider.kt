package com.szastarek.text.rpg.security

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.szastarek.text.rpg.acl.AccountId
import com.szastarek.text.rpg.acl.Role
import com.szastarek.text.rpg.acl.Roles
import com.szastarek.text.rpg.acl.authority.Authority
import com.szastarek.text.rpg.security.config.AuthenticationProperties
import java.util.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class AuthTokenProvider(private val authenticationProperties: AuthenticationProperties) {

    fun createToken(accountId: AccountId, role: Role, customAuthorities: List<Authority>): String =
        JWT.create()
            .withAudience(authenticationProperties.jwtAudience)
            .withIssuer(authenticationProperties.jwtIssuer)
            .withSubject(accountId.value)
            .withClaim("role", Roles.getByRole(role))
            .withClaim("custom_authorities", Json.encodeToString(customAuthorities))
            .withExpiresAt(Date(System.currentTimeMillis() + authenticationProperties.expirationInMillis))
            .sign(Algorithm.HMAC256(authenticationProperties.jwtSecret))
}
