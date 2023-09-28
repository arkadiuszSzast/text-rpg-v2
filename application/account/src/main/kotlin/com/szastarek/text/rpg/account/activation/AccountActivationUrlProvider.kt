package com.szastarek.text.rpg.account.activation

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.szastarek.text.rpg.account.Account
import com.szastarek.text.rpg.account.config.AccountActivationProperties
import com.szastarek.text.rpg.security.JwtProperties
import com.szastarek.text.rpg.security.JwtToken
import io.ktor.http.*
import kotlinx.datetime.Clock
import kotlinx.datetime.toJavaInstant
import org.litote.kmongo.Id

class AccountActivationUrlProvider(
  private val accountActivationProperties: AccountActivationProperties,
  private val clock: Clock
) {

  fun provide(accountId: Id<Account>): Url {
    val baseUrl = accountActivationProperties.activateAccountUrl
    val token = generateToken(accountId, accountActivationProperties.jwtConfig)

    return URLBuilder(baseUrl).apply {
      parameters.append("token", token.value)
    }.build()
  }

  private fun generateToken(accountId: Id<Account>, jwtConfig: JwtProperties): JwtToken {

    return JwtToken(
      JWT.create()
        .withIssuer(jwtConfig.issuer.value)
        .withSubject(accountId.toString())
        .withExpiresAt(clock.now().plus(jwtConfig.expiration).toJavaInstant())
        .sign(Algorithm.HMAC256(jwtConfig.secret.value))
    )
  }
}