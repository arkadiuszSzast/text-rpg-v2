package com.szastarek.text.rpg.account.adapter.rest.request

import kotlinx.serialization.Serializable

@Serializable
data class ActivateAccountRequest(val token: String)
