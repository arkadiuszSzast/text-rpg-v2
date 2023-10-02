package com.szastarek.text.rpg.account.command

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.szastarek.text.rpg.shared.validate.ValidationError
import io.kotest.assertions.arrow.core.shouldBeLeft
import io.kotest.assertions.arrow.core.shouldBeRight
import io.kotest.core.spec.style.DescribeSpec

class ActivateAccountCommandTest : DescribeSpec({

  describe("ActivateAccountCommandTest") {

    it("should return left when JWT is invalid") {
      //arrange
      val invalidJwt = "not-a-jwt-token"

      //act & assert
      ActivateAccountCommand(invalidJwt).shouldBeLeft(
        listOf(
          ValidationError(
            ".token",
            ".validation.invalid_account_activation_token"
          )
        )
      )
    }

    it("should return right when JWT is valid") {
      //arrange
      val jwt = JWT.create().sign(Algorithm.HMAC256("test-secret"))

      //act & assert
      ActivateAccountCommand(jwt).shouldBeRight()
    }
  }
})