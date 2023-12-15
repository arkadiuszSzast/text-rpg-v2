package com.szastarek.text.rpg.mail.config

import com.szastarek.text.rpg.mail.plugin.mailModule
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.koin.KoinExtension
import io.kotest.matchers.shouldBe
import org.koin.test.KoinTest
import org.koin.test.inject

class SendGridPropertiesTest : KoinTest, DescribeSpec() {
	private val sendgridProperties by inject<SendGridProperties>()

	init {

		extension(KoinExtension(mailModule))

		describe("SendgridPropertiesTest") {

			it("should pick correct values from application.conf") {
				// arrange
				val expected =
					SendGridProperties(
						apiKey = "sendgrid-test-api-key",
						host = "test-api.sendgrid.com",
					)

				// act & assert
				sendgridProperties shouldBe expected
			}
		}
	}
}
