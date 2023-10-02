package com.szastarek.text.rpg.mail

import kotlinx.serialization.Serializable

@JvmInline
@Serializable
value class MailVariables(val values: Map<String, String>)