package com.szastarek.text.rpg.security.config

import com.szastarek.text.rpg.security.plugin.securityModule
import io.kotest.core.extensions.Extension
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.koin.KoinExtension
import io.kotest.matchers.shouldBe
import org.koin.test.KoinTest
import org.koin.test.inject

class CorsPropertiesTest : KoinTest, DescribeSpec() {

    private val corsProperties by inject<CorsProperties>()

    override fun extensions(): List<Extension> {
        return listOf(KoinExtension(securityModule))
    }

    init {

        describe("CorsPropertiesTest") {

            it("should pick correct values from application.conf") {
                //arrange
                val expected = CorsProperties(
                    allowedHosts = listOf("test-host-1:3000", "test-host-2:3000")
                )

                //act & assert
                corsProperties shouldBe expected
            }
        }
    }
}
