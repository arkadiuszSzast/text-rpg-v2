package com.szastarek.text.rpg.account.config

import com.szastarek.text.rpg.account.plugin.accountConfigModule
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.koin.KoinExtension
import io.kotest.matchers.shouldBe
import org.koin.test.KoinTest
import org.koin.test.inject

class DocumentationPropertiesTest : KoinTest, DescribeSpec() {
	private val documentationProperties by inject<DocumentationProperties>()

	init {

		extension(KoinExtension(accountConfigModule))

		describe("MonitoringPropertiesTest") {

			it("should pick correct values from application.conf") {
				// arrange
				val expected =
					DocumentationProperties(
						enabled = true,
					)

				// act & assert
				documentationProperties shouldBe expected
			}
		}
	}
}
