package com.szastarek.text.rpg.account.command

import com.szastarek.text.rpg.acl.regularUserAuthenticatedAccountContext
import com.szastarek.text.rpg.shared.aRawPassword
import io.kotest.assertions.arrow.core.shouldBeRight
import io.kotest.core.spec.style.DescribeSpec

class ChangePasswordCommandTest: DescribeSpec({

  describe("ChangePasswordCommandTest") {

    it("should create instance even when current password does not match current password criteria") {
      //arrange && act
      val result = ChangePasswordCommand("short", aRawPassword().value, regularUserAuthenticatedAccountContext)

      //assert
      result.shouldBeRight()
    }
  }
})
