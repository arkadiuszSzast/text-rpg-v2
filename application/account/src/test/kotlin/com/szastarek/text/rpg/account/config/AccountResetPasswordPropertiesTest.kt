package com.szastarek.text.rpg.account.config

import com.szastarek.text.rpg.account.plugin.accountConfigModule
import com.szastarek.text.rpg.security.JwtIssuer
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.koin.KoinExtension
import io.kotest.matchers.shouldBe
import io.ktor.http.Url
import org.koin.test.KoinTest
import org.koin.test.inject
import kotlin.time.Duration.Companion.milliseconds

class AccountResetPasswordPropertiesTest : KoinTest, DescribeSpec() {
	private val resetPasswordProperties by inject<AccountResetPasswordProperties>()

	init {
		extensions(KoinExtension(accountConfigModule))

		describe("AccountResetPasswordPropertiesTest") {

			it("should pick correct values from application.conf") {
				// arrange
				val expected =
					AccountResetPasswordProperties(
						accountResetPasswordUrl = Url("http://test-host:3000/account/reset-password"),
						jwtIssuer = JwtIssuer("reset-password-jwt-test-issuer"),
						jwtExpiration = 300000.milliseconds,
					)

				// act & assert
				resetPasswordProperties shouldBe expected
			}
		}
	}
}
