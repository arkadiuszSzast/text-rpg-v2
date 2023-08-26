package com.szastarek.text.rpg.mediator

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe

class KediatrRequestTypeExtractorTest : DescribeSpec({

    describe("KediatrRequestTypeExtractor") {

        it("should extract command type") {
            // arrange && act
            val result = KediatrRequestTypeExtractor.extract(SimpleCommand())

            // assert
           result shouldBe KediatrRequestType.Command
        }

        it("should extract commandWithResult type") {
            // arrange && act
            val result = KediatrRequestTypeExtractor.extract(SimpleCommandWithResult())

            // assert
            result shouldBe KediatrRequestType.CommandWithResult
        }

        it("should extract query type") {
            // arrange && act
            val result = KediatrRequestTypeExtractor.extract(SimpleQuery())

            // assert
            result shouldBe KediatrRequestType.Query
        }

        it("should extract notification type") {
            // arrange && act
            val result = KediatrRequestTypeExtractor.extract(SimpleNotification())

            // assert
            result shouldBe KediatrRequestType.Notification
        }

        it("should return unknown when not found") {
            // arrange && act
            val result = KediatrRequestTypeExtractor.extract(UnknownRequest())

            // assert
            result shouldBe KediatrRequestType.Unknown
        }
    }
})
