package com.szastarek.text.rpg.acl

import com.szastarek.text.rpg.acl.authority.Authority
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
sealed interface Role

@Serializable
@SerialName("RegularRole")
data class RegularRole(val name: String, val authorities: List<Authority>) : Role

@Serializable
@SerialName("SuperUserRole")
data object SuperUserRole : Role
