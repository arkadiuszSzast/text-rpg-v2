package com.szastarek.text.rpg.account.world.creator

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.szastarek.text.rpg.account.config.WorldCreatorRegisterProperties
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
import io.ktor.http.Url
import kotlinx.datetime.Clock
import kotlinx.datetime.toJavaInstant
import java.time.temporal.ChronoUnit
import kotlin.time.Duration.Companion.milliseconds

class WorldCreatorRegisterUrlProviderTest : DescribeSpec({

	describe("WorldCreatorRegisterUrlProviderTest") {

		val clock = FixedClock(Clock.System.now())
		val worldCreatorRegisterProperties =
			WorldCreatorRegisterProperties(
				registerUrl = Url("http://test-host:3000/account/world-creator"),
				jwtConfig =
					JwtProperties(
						JwtSecret("world-creator-register-jwt-test-secret"),
						JwtIssuer("world-creator-register-jwt-test-issuer"),
						3600000.milliseconds,
					),
			)
		val worldCreatorRegisterUrlProvider = WorldCreatorRegisterUrlProvider(worldCreatorRegisterProperties, clock)

		it("should provide world creator register url") {
			// arrange
			val jwtConfig = worldCreatorRegisterProperties.jwtConfig
			val emailAddress = anEmail()

			// act
			val result = worldCreatorRegisterUrlProvider.provide(emailAddress)

			result.toString().shouldStartWith(worldCreatorRegisterProperties.registerUrl.toString())
			result.parameters["token"].shouldNotBeNull().should {
				val decodedToken = JWT.decode(it)
				val jwtVerifier = JWT.require(Algorithm.HMAC256(jwtConfig.secret.value)).build()
				shouldNotThrowAny {
					jwtVerifier.verify(decodedToken)
				}
				decodedToken.subject shouldBe emailAddress.value
				decodedToken.issuer shouldBe jwtConfig.issuer.value
				decodedToken.expiresAtAsInstant.shouldBe(
					clock.now().plus(jwtConfig.expiration).toJavaInstant().truncatedTo(ChronoUnit.SECONDS),
				)
			}
		}
	}
})
