package com.szastarek.text.rpg.account.world.creator

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.szastarek.text.rpg.account.config.WorldCreatorRegisterProperties
import com.szastarek.text.rpg.security.JwtProperties
import com.szastarek.text.rpg.security.JwtToken
import com.szastarek.text.rpg.shared.email.EmailAddress
import io.ktor.http.URLBuilder
import io.ktor.http.Url
import kotlinx.datetime.Clock
import kotlinx.datetime.toJavaInstant

class WorldCreatorRegisterUrlProvider(
  private val worldCreatorRegisterProperties: WorldCreatorRegisterProperties,
  private val clock: Clock
) {

  fun provide(emailAddress: EmailAddress): Url {
    val baseUrl = worldCreatorRegisterProperties.registerUrl
    val token = generateToken(emailAddress, worldCreatorRegisterProperties.jwtConfig)

    return URLBuilder(baseUrl).apply {
      parameters.append("token", token.value)
    }.build()
  }

  private fun generateToken(emailAddress: EmailAddress, jwtConfig: JwtProperties): JwtToken {

    return JwtToken(
      JWT.create()
        .withIssuer(jwtConfig.issuer.value)
        .withSubject(emailAddress.value)
        .withExpiresAt(clock.now().plus(jwtConfig.expiration).toJavaInstant())
        .sign(Algorithm.HMAC256(jwtConfig.secret.value))
    )
  }
}