package com.szastarek.text.rpg.security

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.szastarek.text.rpg.acl.AccountId
import com.szastarek.text.rpg.acl.Role
import com.szastarek.text.rpg.acl.Roles
import com.szastarek.text.rpg.acl.authority.Authority
import com.szastarek.text.rpg.security.config.AuthenticationProperties
import com.szastarek.text.rpg.shared.email.EmailAddress
import kotlinx.datetime.Clock
import kotlinx.datetime.toJavaInstant
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class AuthTokenProvider(private val authenticationProperties: AuthenticationProperties, private val clock: Clock) {

    fun createAuthToken(accountId: AccountId, emailAddress: EmailAddress, role: Role, customAuthorities: List<Authority>): JwtToken =
        JwtToken(JWT.create()
            .withAudience(authenticationProperties.jwtAudience)
            .withIssuer(authenticationProperties.jwtIssuer)
            .withSubject(accountId.value)
            .withClaim("email", emailAddress.value)
            .withClaim("role", Roles.getByRole(role))
            .withClaim("custom_authorities", Json.encodeToString(customAuthorities))
            .withExpiresAt(clock.now().plus(authenticationProperties.authTokenExpiration).toJavaInstant())
            .sign(Algorithm.HMAC256(authenticationProperties.jwtSecret)))
}
