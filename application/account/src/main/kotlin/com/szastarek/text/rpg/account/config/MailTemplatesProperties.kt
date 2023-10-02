package com.szastarek.text.rpg.account.config

import com.szastarek.text.rpg.mail.MailSubject
import com.szastarek.text.rpg.mail.MailTemplateId
import com.szastarek.text.rpg.shared.email.EmailAddress

data class ActivateAccountMailProperties(
  val templateId: MailTemplateId,
  val sender: EmailAddress,
  val subject: MailSubject
)

data class ResetPasswordMailProperties(
  val templateId: MailTemplateId,
  val sender: EmailAddress,
  val subject: MailSubject
)

data class InviteWorldCreatorMailProperties(
  val templateId: MailTemplateId,
  val sender: EmailAddress,
  val subject: MailSubject
)