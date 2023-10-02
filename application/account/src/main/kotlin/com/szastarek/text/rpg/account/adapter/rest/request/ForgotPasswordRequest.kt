package com.szastarek.text.rpg.account.adapter.rest.request

import kotlinx.serialization.Serializable

@Serializable
data class ForgotPasswordRequest(val email: String)
