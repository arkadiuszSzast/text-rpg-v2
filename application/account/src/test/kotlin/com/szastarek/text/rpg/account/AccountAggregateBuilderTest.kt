package com.szastarek.text.rpg.account

import arrow.core.nonEmptyListOf
import com.szastarek.text.rpg.account.support.anAccountCreatedEvent
import io.kotest.assertions.arrow.core.shouldBeRight
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.should
import io.kotest.matchers.shouldBe

class AccountAggregateBuilderTest : DescribeSpec({

  describe("AccountAggregateBuilderTest") {

    it("should rebuild account aggregate from events") {
      //arrange
      val accountCreatedEvent = anAccountCreatedEvent()
      val events = nonEmptyListOf(accountCreatedEvent)

      //act
      val result = AccountAggregateBuilder().apply(events)

      //assert
      result.shouldBeRight() should {
        it.id shouldBe accountCreatedEvent.accountId
        it.emailAddress shouldBe accountCreatedEvent.emailAddress
        it.status shouldBe accountCreatedEvent.status
        it.role shouldBe accountCreatedEvent.role
        it.customAuthorities shouldBe accountCreatedEvent.customAuthorities
        it.createdAt shouldBe accountCreatedEvent.createdAt
        it.password shouldBe accountCreatedEvent.password
        it.timeZone shouldBe accountCreatedEvent.timeZone
      }

    }
  }
})
