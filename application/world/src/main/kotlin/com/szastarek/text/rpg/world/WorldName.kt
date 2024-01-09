package com.szastarek.text.rpg.world

import arrow.core.nel
import arrow.core.raise.either
import arrow.core.raise.ensure
import com.szastarek.text.rpg.shared.validate.ValidationError
import kotlinx.serialization.Serializable

@JvmInline
@Serializable
value class WorldName private constructor(val value: String) {
	companion object {
		operator fun invoke(value: String) =
			either {
				val trimmed = value.trim()
				ensure(trimmed.length >= 3) { ValidationError(".world_name", "validation.world_name_too_short").nel() }
				ensure(trimmed.length <= 30) { ValidationError(".world_name", "validation.world_name_too_long").nel() }
				WorldName(trimmed.trim())
			}
	}
}
