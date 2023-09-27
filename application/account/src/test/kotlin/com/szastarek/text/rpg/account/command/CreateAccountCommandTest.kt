package com.szastarek.text.rpg.account.command

import com.szastarek.text.rpg.shared.aRawPassword
import com.szastarek.text.rpg.shared.anEmail
import io.kotest.assertions.arrow.core.shouldBeLeft
import io.kotest.assertions.arrow.core.shouldBeRight
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import kotlinx.datetime.TimeZone

class CreateAccountCommandTest : DescribeSpec({

    describe("CreateAccountCommandTest") {

        it("should aggregate errors") {
            //arrange & act
            val command = CreateRegularAccountCommand("invalid-mail", "short", "invalid-timezone")
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
            val command = CreateRegularAccountCommand(
                anEmail().value,
                aRawPassword().value,
                TimeZone.availableZoneIds.random()
            )

            //assert
            command.shouldBeRight()
        }
    }

})
