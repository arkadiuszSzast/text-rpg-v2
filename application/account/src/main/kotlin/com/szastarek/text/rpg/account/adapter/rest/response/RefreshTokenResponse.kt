package com.szastarek.text.rpg.account.adapter.rest.response

import kotlinx.serialization.Serializable

@Serializable
data class RefreshTokenResponse(val authToken: String, val refreshToken: String)
