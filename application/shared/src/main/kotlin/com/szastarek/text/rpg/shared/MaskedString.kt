package com.szastarek.text.rpg.shared

import kotlinx.serialization.Serializable

@JvmInline
@Serializable
value class MaskedString(val value: String) {
	override fun toString(): String {
		return "*masked*"
	}
}
