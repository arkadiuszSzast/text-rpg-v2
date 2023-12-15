package com.szastarek.text.rpg.acl.authority

sealed interface Decision {
	fun toBoolean(): Boolean
}

data object Allow : Decision {
	override fun toBoolean() = true
}

data class Deny(val reason: Throwable) : Decision {
	override fun toBoolean() = false
}
