package com.szastarek.text.rpg.acl.authority

import kotlinx.serialization.Serializable

@Serializable
enum class AuthorityLevel {
    View,
    Create,
    Update,
    Delete,
    Manage
}
