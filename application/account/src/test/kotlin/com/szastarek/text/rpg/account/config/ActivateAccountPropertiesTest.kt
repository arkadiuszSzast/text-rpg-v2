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

class ActivateAccountPropertiesTest : KoinTest, DescribeSpec() {
	private val activateAccountProperties by inject<AccountActivationProperties>()

	init {
		extensions(KoinExtension(accountConfigModule))

		describe("ActivateAccountPropertiesTest") {

			it("should pick correct values from application.conf") {
				// arrange
				val expected =
					AccountActivationProperties(
						activateAccountUrl = Url("http://test-host:3000/account/activate"),
						jwtConfig =
							JwtProperties(
								JwtSecret("activate-account-jwt-test-secret"),
								JwtIssuer("activate-account-jwt-test-issuer"),
								3600000.milliseconds,
							),
					)

				// act & assert
				activateAccountProperties shouldBe expected
			}
		}
	}
}
