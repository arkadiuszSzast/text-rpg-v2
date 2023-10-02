package com.szastarek.text.rpg.account.activation

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.szastarek.text.rpg.account.config.AccountActivationProperties
import com.szastarek.text.rpg.security.JwtIssuer
import com.szastarek.text.rpg.security.JwtProperties
import com.szastarek.text.rpg.security.JwtSecret
import com.szastarek.text.rpg.shared.anEmail
import com.szastarek.text.rpg.utils.FixedClock
import io.kotest.assertions.throwables.shouldNotThrowAny
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.should
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldStartWith
import io.ktor.http.*
import kotlinx.datetime.Clock
import kotlinx.datetime.toJavaInstant
import java.time.temporal.ChronoUnit
import kotlin.time.Duration.Companion.milliseconds

class AccountActivationUrlProviderTest : DescribeSpec({

  describe("AccountActivationUrlProviderTest") {

    val clock = FixedClock(Clock.System.now())
    val accountActivationProperties = AccountActivationProperties(
      activateAccountUrl = Url("http://test-host:3000/account/activate"),
      jwtConfig = JwtProperties(
        JwtSecret("activate-account-jwt-test-secret"),
        JwtIssuer("activate-account-jwt-test-issuer"),
        3600000.milliseconds
      )
    )
    val accountActivationUrlProvider = AccountActivationUrlProvider(accountActivationProperties, clock)

    it("should provide activation url") {
      //arrange
      val jwtConfig = accountActivationProperties.jwtConfig
      val emailAddress = anEmail()

      //act
      val result = accountActivationUrlProvider.provide(emailAddress)

      result.toString().shouldStartWith(accountActivationProperties.activateAccountUrl.toString())
      result.parameters["token"].shouldNotBeNull().should {
        val decodedToken = JWT.decode(it)
        val jwtVerifier = JWT.require(Algorithm.HMAC256(jwtConfig.secret.value)).build()
        shouldNotThrowAny {
          jwtVerifier.verify(decodedToken)
        }
        decodedToken.subject shouldBe emailAddress.value
        decodedToken.issuer shouldBe jwtConfig.issuer.value
        decodedToken.expiresAtAsInstant.shouldBe(
          clock.now().plus(jwtConfig.expiration).toJavaInstant().truncatedTo(ChronoUnit.SECONDS)
        )
      }
    }
  }
})
