package com.szastarek.text.rpg.shared.email

import io.kotest.assertions.arrow.core.shouldBeLeft
import io.kotest.assertions.arrow.core.shouldBeRight
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe

class EmailAddressTest : DescribeSpec({

    describe("EmailAddressTest") {

            it("should create email address") {
                // arrange & act
                val email = EmailAddress("test@test.com")

                //assert
                email.shouldBeRight()
            }

        it("should trim email address") {
            // arrange & act
            val email = EmailAddress("  test@test.com  ")

            //assert
            email.shouldBeRight()
                .value shouldBe "test@test.com"
        }

        it("should not create email address") {
            // arrange & act
            val email = EmailAddress("test@test")

            //assert
            email.shouldBeLeft().map { it.message } shouldBe listOf("validation.invalid_email")
        }
    }

})
