package com.szastarek.text.rpg.mail.adapter.sendgrid

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe

class SendgridPersonalizationTest : DescribeSpec({

	describe("SendgridPersonalizationTest") {

		it("should add subject to dynamicTemplateData") {
			// arrange & act
			val result =
				SendgridPersonalization(
					to = listOf(SendgridEmail("test@test.com")),
					subject = "subject-test",
					dynamicTemplateData = mapOf("test-data" to "test-value"),
				)

			// assert
			result.dynamicTemplateData shouldBe
				mapOf(
					"test-data" to "test-value",
					"subject" to "subject-test",
				)
		}
	}
})
