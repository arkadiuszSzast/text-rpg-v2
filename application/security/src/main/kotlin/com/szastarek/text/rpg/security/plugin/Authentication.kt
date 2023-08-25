package com.szastarek.text.rpg.security.plugin

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.szastarek.text.rpg.security.config.AuthenticationProperties
import com.szastarek.text.rpg.shared.plugin.installIfNotRegistered
import io.ktor.server.application.Application
import io.ktor.server.auth.Authentication
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.jwt.jwt

fun Application.configureAuthentication(authenticationProperties: AuthenticationProperties) {
    installIfNotRegistered(Authentication) {
        jwt {
            realm = authenticationProperties.jwtRealm
            verifier(
                JWT
                    .require(Algorithm.HMAC256(authenticationProperties.jwtSecret))
                    .withAudience(authenticationProperties.jwtAudience)
                    .withIssuer(authenticationProperties.jwtIssuer)
                    .build()
            )
            validate { credential ->
                if (credential.payload.audience.contains(authenticationProperties.jwtAudience)) {
                    JWTPrincipal(credential.payload)
                } else {
                    null
                }
            }
        }
    }
}
