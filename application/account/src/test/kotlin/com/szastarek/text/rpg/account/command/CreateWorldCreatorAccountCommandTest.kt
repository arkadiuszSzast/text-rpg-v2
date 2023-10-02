package com.szastarek.text.rpg.account.command

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.szastarek.text.rpg.shared.aRawPassword
import com.szastarek.text.rpg.shared.anEmail
import io.kotest.assertions.arrow.core.shouldBeLeft
import io.kotest.assertions.arrow.core.shouldBeRight
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import kotlinx.datetime.TimeZone

class CreateWorldCreatorAccountCommandTest : DescribeSpec({

  describe("CreateWorldCreatorAccountCommandTest") {

    it("should aggregate errors") {
      //arrange & act
      val command = CreateWorldCreatorAccountCommand("invalid-mail", "short", "invalid-timezone", "invalid-token")
      val expectedMessages = listOf(
        "validation.invalid_email",
        "validation.password_too_short",
        "validation.invalid_invite_world_creator_token",
        "validation.invalid_timezone"
      )

      //assert
      command.shouldBeLeft().map { it.message } shouldBe expectedMessages
    }

    it("should create command") {
      //arrange & act
      val command = CreateWorldCreatorAccountCommand(
        anEmail().value,
        aRawPassword().value,
        TimeZone.availableZoneIds.random(),
        JWT.create().sign(Algorithm.HMAC256("secret"))
      )

      //assert
      command.shouldBeRight()
    }
  }

})