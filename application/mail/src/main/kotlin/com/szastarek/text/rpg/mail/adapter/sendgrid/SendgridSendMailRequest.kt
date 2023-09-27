package com.szastarek.text.rpg.mail.adapter.sendgrid

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class SendgridSendMailRequest(
    val from: SendgridEmail,
    @SerialName("template_id")
    val templateId: String,
    @SerialName("personalizations")
    val personalization: List<SendgridPersonalization>
)

@Serializable
internal data class SendgridEmail(val email: String)

@Serializable
internal data class SendgridPersonalization private constructor(
    val to: List<SendgridEmail>,
    @SerialName("dynamic_template_data")
    val dynamicTemplateData: Map<String, String>
) {
    constructor(to: List<SendgridEmail>, subject: String, dynamicTemplateData: Map<String, String>) : this(
        to = to,
        dynamicTemplateData = dynamicTemplateData + ("subject" to subject)
    )
}
