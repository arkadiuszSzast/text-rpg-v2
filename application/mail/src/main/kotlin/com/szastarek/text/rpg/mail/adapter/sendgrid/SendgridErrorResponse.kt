package com.szastarek.text.rpg.mail.adapter.sendgrid

import kotlinx.serialization.Serializable

@Serializable
data class SendgridErrorResponse(val errors: List<SendgridError>)

@Serializable
data class SendgridError(val field: String, val message: String)
