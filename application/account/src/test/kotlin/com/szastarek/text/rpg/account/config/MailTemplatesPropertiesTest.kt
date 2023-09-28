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

    private val mailTemplatesProperties by inject<MailTemplatesProperties>()

    init {
        extensions(KoinExtension(accountConfigModule))

        describe("MailTemplatesPropertiesTest") {

            it("should pick correct values from application.conf") {
                //arrange
                val expected = MailTemplatesProperties(
                    activateAccount = MailTemplate(
                        templateId = MailTemplateId("activate-account-test-templateId"),
                        sender = EmailAddress("test-sender@mail.com").getOrThrow(),
                        subject = MailSubject("activate-account-test-subject")
                    )
                )

                //act & assert
                mailTemplatesProperties shouldBe expected
            }
        }
    }
}
