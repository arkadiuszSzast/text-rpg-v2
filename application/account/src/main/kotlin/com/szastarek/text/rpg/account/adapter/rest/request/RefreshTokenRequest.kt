package com.szastarek.text.rpg.account.adapter.rest.request

import kotlinx.serialization.Serializable

@Serializable
data class RefreshTokenRequest(val refreshToken: String, val accountEmail: String)
