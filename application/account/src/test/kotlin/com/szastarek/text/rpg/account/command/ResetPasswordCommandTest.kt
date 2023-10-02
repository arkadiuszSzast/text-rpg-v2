package com.szastarek.text.rpg.account.command

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.szastarek.text.rpg.shared.aRawPassword
import com.szastarek.text.rpg.shared.validate.ValidationError
import io.kotest.assertions.arrow.core.shouldBeLeft
import io.kotest.core.spec.style.DescribeSpec

class ResetPasswordCommandTest : DescribeSpec({

  describe("ResetPasswordCommandTest") {

    it("should return right") {
      //arrange
      val token = JWT.create().sign(Algorithm.HMAC256("secret"))

      //act
      val command = ResetPasswordCommand(token, aRawPassword().value)

      //assert
      command.isRight()
    }

    it("should accumulate errors") {
      //arrange & act
      val command = ResetPasswordCommand("invalid-token", "short")

      //assert
      command.shouldBeLeft(
        listOf(
          ValidationError(".token", "validation.invalid_reset_password_token"),
          ValidationError(".password", "validation.password_too_short"),
        )
      )
    }
  }
})
