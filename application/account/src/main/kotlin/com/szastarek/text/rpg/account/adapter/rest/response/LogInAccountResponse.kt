package com.szastarek.text.rpg.account.adapter.rest.response

import kotlinx.serialization.Serializable

@Serializable
data class LogInAccountResponse(val authToken: String)
