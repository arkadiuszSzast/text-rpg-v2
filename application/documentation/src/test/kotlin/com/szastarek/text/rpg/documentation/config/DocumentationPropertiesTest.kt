package com.szastarek.text.rpg.documentation.config

import com.szastarek.text.rpg.documentation.plugin.documentationModule
import io.kotest.core.extensions.Extension
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.koin.KoinExtension
import io.kotest.matchers.shouldBe
import org.koin.test.KoinTest
import org.koin.test.inject

class DocumentationPropertiesTest : KoinTest, DescribeSpec() {

    private val documentationProperties by inject<DocumentationProperties>()

    override fun extensions(): List<Extension> {
        return listOf(KoinExtension(documentationModule))
    }

    init {

        describe("MonitoringPropertiesTest") {

            it("should pick correct values from application.conf") {
                //arrange
                val expected = DocumentationProperties(
                    enabled = true,
                )

                //act & assert
                documentationProperties shouldBe expected
            }
        }
    }
}
