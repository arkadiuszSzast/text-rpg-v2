package com.szastarek.text.rpg.account.config

import com.szastarek.text.rpg.mail.MailSubject
import com.szastarek.text.rpg.mail.MailTemplateId
import com.szastarek.text.rpg.shared.email.EmailAddress

data class MailTemplatesProperties(val activateAccount: MailTemplate)

data class MailTemplate(
  val templateId: MailTemplateId,
  val sender: EmailAddress,
  val subject: MailSubject
)
