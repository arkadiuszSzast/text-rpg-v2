package com.szastarek.text.rpg.shared.config

import com.szastarek.text.rpg.shared.plugin.sharedKoinModule
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.koin.KoinExtension
import io.kotest.matchers.shouldBe
import org.koin.test.KoinTest
import org.koin.test.inject

class ApplicationPropertiesTest : KoinTest, DescribeSpec() {

    private val applicationProperties by inject<ApplicationProperties>()

    init {

        extensions(KoinExtension(sharedKoinModule))

        describe("ApplicationPropertiesTest") {

            it("should pick correct values from application.conf") {
                //arrange
                val expected = ApplicationProperties(
                    environment = "test",
                    webClientAppUrl = "http://test-client-host:3000"
                )

                //act & assert
                applicationProperties shouldBe expected
            }
        }
    }
}
