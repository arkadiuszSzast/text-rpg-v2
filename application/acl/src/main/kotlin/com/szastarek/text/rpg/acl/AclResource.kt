package com.szastarek.text.rpg.acl

import kotlinx.serialization.Serializable

interface AclResource {
	val aclResourceIdentifier: AclResourceIdentifier
}

@JvmInline
@Serializable
value class AclResourceIdentifier(val name: String)
