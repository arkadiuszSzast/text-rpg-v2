package com.szastarek.text.rpg.world

import arrow.core.nel
import arrow.core.raise.either
import arrow.core.raise.ensure
import com.szastarek.text.rpg.shared.validate.ValidationError
import kotlinx.serialization.Serializable

@JvmInline
@Serializable
value class WorldDescription private constructor(val value: String) {
	companion object {
		operator fun invoke(value: String) =
			either {
				val trimmed = value.trim()
				ensure(trimmed.length >= 10) { ValidationError(".world_description", "validation.world_description_too_short").nel() }
				ensure(trimmed.length <= 5000) { ValidationError(".world_description", "validation.world_description_too_long").nel() }
				WorldDescription(trimmed)
			}
	}
}
