package com.szastarek.text.rpg.account.activation

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.szastarek.text.rpg.account.config.AccountActivationProperties
import com.szastarek.text.rpg.security.JwtIssuer
import com.szastarek.text.rpg.security.JwtProperties
import com.szastarek.text.rpg.security.JwtSecret
import com.szastarek.text.rpg.security.JwtToken
import com.szastarek.text.rpg.shared.anEmail
import com.szastarek.text.rpg.utils.FixedClock
import io.kotest.assertions.arrow.core.shouldBeLeft
import io.kotest.assertions.arrow.core.shouldBeRight
import io.kotest.core.spec.style.DescribeSpec
import io.ktor.http.*
import kotlinx.datetime.Clock
import kotlinx.datetime.toJavaInstant
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.milliseconds

class AccountActivationTokenVerifierTest : DescribeSpec({

  describe("AccountActivationTokenVerifierTest") {

    val clock = FixedClock(Clock.System.now())
    val accountActivationProperties = AccountActivationProperties(
      activateAccountUrl = Url("http://test-host:3000/account/activate"),
      jwtConfig = JwtProperties(
        JwtSecret("activate-account-jwt-test-secret"),
        JwtIssuer("activate-account-jwt-test-issuer"),
        3600000.milliseconds
      )
    )
    val accountActivationTokenVerifier = AccountActivationTokenVerifier(accountActivationProperties)
    val accountActivationUrlProvider = AccountActivationUrlProvider(accountActivationProperties, clock)

    it("should verify valid token") {
      //given
      val token = JwtToken(accountActivationUrlProvider.provide(anEmail()).parameters["token"]!!)

      //act & assert
      accountActivationTokenVerifier.verify(token).shouldBeRight()
    }

    it("should return left when secret does not match") {
      //arrange
      val jwtConfig = accountActivationProperties.jwtConfig
      val token = JwtToken(
        JWT.create()
          .withIssuer(jwtConfig.issuer.value)
          .withSubject(anEmail().value)
          .withExpiresAt(clock.now().plus(jwtConfig.expiration).toJavaInstant())
          .sign(Algorithm.HMAC256("invalid-secret"))
      )

      //act
      accountActivationTokenVerifier.verify(token).shouldBeLeft(InvalidJwtResult)
    }

    it("should return left when issuer does not match") {
      //arrange
      val jwtConfig = accountActivationProperties.jwtConfig
      val token = JwtToken(
        JWT.create()
          .withIssuer("invalid-issuer")
          .withSubject(anEmail().value)
          .withExpiresAt(clock.now().plus(jwtConfig.expiration).toJavaInstant())
          .sign(Algorithm.HMAC256(jwtConfig.secret.value))
      )

      //act
      accountActivationTokenVerifier.verify(token).shouldBeLeft(InvalidJwtResult)
    }


    it("should return left when token is expired") {
      //arrange
      val jwtConfig = accountActivationProperties.jwtConfig
      val token = JwtToken(
        JWT.create()
          .withIssuer(jwtConfig.issuer.value)
          .withSubject(anEmail().value)
          .withExpiresAt(clock.now().minus(1.hours).toJavaInstant())
          .sign(Algorithm.HMAC256(jwtConfig.secret.value))
      )

      //act
      accountActivationTokenVerifier.verify(token).shouldBeLeft(InvalidJwtResult)
    }
  }
})