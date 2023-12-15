package com.szastarek.text.rpg.account.config

import com.szastarek.text.rpg.account.plugin.accountConfigModule
import com.szastarek.text.rpg.mail.MailSubject
import com.szastarek.text.rpg.mail.MailTemplateId
import com.szastarek.text.rpg.shared.email.EmailAddress
import com.szastarek.text.rpg.shared.validate.getOrThrow
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.koin.KoinExtension
import io.kotest.matchers.shouldBe
import org.koin.test.KoinTest
import org.koin.test.inject

class MailTemplatesPropertiesTest : KoinTest, DescribeSpec() {
	private val activateAccountTemplatesProperties by inject<ActivateAccountMailProperties>()
	private val resetPasswordTemplatesProperties by inject<ResetPasswordMailProperties>()
	private val inviteWorldCreatorMailProperties by inject<InviteWorldCreatorMailProperties>()

	init {
		extensions(KoinExtension(accountConfigModule))

		describe("MailTemplatesPropertiesTest") {

			it("should pick correct values from application.conf") {
				// arrange
				val expectedActivateAccountMailProperties =
					ActivateAccountMailProperties(
						templateId = MailTemplateId("activate-account-test-templateId"),
						sender = EmailAddress("activate-account-test-sender@mail.com").getOrThrow(),
						subject = MailSubject("activate-account-test-subject"),
					)
				val expectResetPasswordMailProperties =
					ResetPasswordMailProperties(
						templateId = MailTemplateId("reset-password-test-templateId"),
						sender = EmailAddress("reset-password-test-sender@mail.com").getOrThrow(),
						subject = MailSubject("reset-password-test-subject"),
					)
				val expectInviteWorldCreatorMailProperties =
					InviteWorldCreatorMailProperties(
						templateId = MailTemplateId("invite-world-creator-test-templateId"),
						sender = EmailAddress("invite-world-creator-test-sender@mail.com").getOrThrow(),
						subject = MailSubject("invite-world-creator-test-subject"),
					)

				// act & assert
				activateAccountTemplatesProperties shouldBe expectedActivateAccountMailProperties
				resetPasswordTemplatesProperties shouldBe expectResetPasswordMailProperties
				inviteWorldCreatorMailProperties shouldBe expectInviteWorldCreatorMailProperties
			}
		}
	}
}
