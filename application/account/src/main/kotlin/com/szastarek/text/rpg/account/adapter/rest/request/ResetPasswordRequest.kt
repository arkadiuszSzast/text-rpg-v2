package com.szastarek.text.rpg.account.adapter.rest.request

import com.szastarek.text.rpg.shared.MaskedString
import kotlinx.serialization.Serializable

@Serializable
data class ResetPasswordRequest(val token: String, val newPassword: MaskedString)
