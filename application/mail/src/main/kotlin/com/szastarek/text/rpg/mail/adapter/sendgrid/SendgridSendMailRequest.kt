package com.szastarek.text.rpg.mail.adapter.sendgrid

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class SendgridSendMailRequest(
  val from: SendgridEmail,
  val subject: String,
  @SerialName("template_id")
  val templateId: String,
  val personalizations: List<SendgridPersonalization>
)

@Serializable
internal data class SendgridEmail(val email: String)

@Serializable
internal data class SendgridPersonalization(
  val to: List<SendgridEmail>,
  @SerialName("dynamic_template_data")
  val dynamicTemplateData: Map<String, String>
)