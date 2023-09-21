package com.szastarek.text.rpg.account.command

import io.kotest.assertions.arrow.core.shouldBeLeft
import io.kotest.assertions.arrow.core.shouldBeRight
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe

class CreateAccountCommandTest : DescribeSpec({

    describe("CreateAccountCommandTest") {

        it("should aggregate errors") {
            //arrange & act
            val command = CreateAccountCommand("invalid-mail", "short", "invalid-timezone")
            val expectedMessages = listOf(
                "validation.invalid_email",
                "validation.password_too_short",
                "validation.invalid_timezone"
            )

            //assert
            command.shouldBeLeft().map { it.message } shouldBe expectedMessages
        }

        it("should create command") {
            //arrange & act
            val command = CreateAccountCommand("test@email.com", "secret-password", "Europe/Warsaw")

            //assert
            command.shouldBeRight()
        }
    }

})
