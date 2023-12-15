package com.szastarek.text.rpg.account.config

import com.szastarek.text.rpg.account.plugin.accountConfigModule
import com.szastarek.text.rpg.security.JwtIssuer
import com.szastarek.text.rpg.security.JwtProperties
import com.szastarek.text.rpg.security.JwtSecret
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.koin.KoinExtension
import io.kotest.matchers.shouldBe
import io.ktor.http.Url
import org.koin.test.KoinTest
import org.koin.test.inject
import kotlin.time.Duration.Companion.milliseconds

class WorldCreatorRegisterUrlPropertiesTest : KoinTest, DescribeSpec() {
	private val worldCreatorRegisterProperties by inject<WorldCreatorRegisterProperties>()

	init {
		extensions(KoinExtension(accountConfigModule))

		describe("WorldCreatorRegisterUrlPropertiesTest") {

			it("should pick correct values from application.conf") {
				// arrange
				val expected =
					WorldCreatorRegisterProperties(
						registerUrl = Url("http://test-host:3000/account/world-creator"),
						jwtConfig =
							JwtProperties(
								JwtSecret("world-creator-register-jwt-test-secret"),
								JwtIssuer("world-creator-register-jwt-test-issuer"),
								60000.milliseconds,
							),
					)

				// act & assert
				worldCreatorRegisterProperties shouldBe expected
			}
		}
	}
}
