package com.szastarek.text.rpg.shared

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe

class VersionTest : DescribeSpec({

    describe("VersionTest") {

        it("initial version should be 0") {
            //arrange & act
            val result = Version.initial

            //assert
            result shouldBe Version(0)
        }

        it("should generate next version") {
            //arrange
            val version = Version(1)

            //act
            val result = version.next()

            //assert
            result shouldBe Version(2)
        }

        it("should throw exception when version is less than 0") {
            //arrange & act & assert
            shouldThrow<IllegalArgumentException> {
                Version(-1)
            }
        }
    }

})
