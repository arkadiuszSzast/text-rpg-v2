package com.szastarek.text.rpg.account.adapter.rest.request

import kotlinx.serialization.Serializable

@Serializable
data class CreateWorldCreatorAccountRequest(val email: String, val password: String, val timeZoneId: String, val token: String)
