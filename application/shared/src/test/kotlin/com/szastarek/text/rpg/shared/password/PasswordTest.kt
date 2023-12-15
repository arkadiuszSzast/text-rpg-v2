package com.szastarek.text.rpg.shared.password

import io.kotest.assertions.arrow.core.shouldBeLeft
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe

class PasswordTest : DescribeSpec({

	describe("PasswordTest") {

		it("should hash password and compare") {
			// arrange
			val notHashedPassword = "Super_secret1!"

			// act
			val password = RawPassword(notHashedPassword).getOrNull()!!.hashpw()

			// assert
			password.matches(RawPassword(notHashedPassword).getOrNull()!!) shouldBe true
		}

		it("should return invalid when password is too short") {
			// arrange && act
			val password = RawPassword("short")

			// assert
			password.shouldBeLeft().map { it.message } shouldBe
				listOf(
					"validation.password_too_short",
				)
		}
	}
})
