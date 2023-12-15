package com.szastarek.text.rpg.account.adapter.rest.response

import com.szastarek.text.rpg.acl.Role
import kotlinx.serialization.Serializable

@Serializable
data class AccountDetailsResponse(
	val accountId: String,
	val email: String,
	val role: Role,
)
