package com.szastarek.text.rpg.mail

import com.szastarek.text.rpg.shared.email.EmailAddress
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import org.litote.kmongo.Id

@Serializable
data class Mail(
  @Contextual val id: Id<Mail>,
  val subject: MailSubject,
  val from: EmailAddress,
  val to: EmailAddress,
  val templateId: MailTemplateId,
  val variables: MailVariables
)
