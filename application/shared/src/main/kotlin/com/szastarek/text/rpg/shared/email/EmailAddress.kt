package com.szastarek.text.rpg.shared.email

import arrow.core.nel
import arrow.core.raise.either
import arrow.core.raise.ensure
import com.szastarek.text.rpg.shared.validate.ValidationError
import kotlinx.serialization.Serializable

@JvmInline
@Serializable
value class EmailAddress private constructor(val value: String) {
	companion object {
		operator fun invoke(address: String) =
			either {
				ensure(address.matches(Regex(EMAIL_PATTERN))) { ValidationError(".email", "validation.invalid_email").nel() }

				EmailAddress(address.trim())
			}
	}
}

private const val EMAIL_PATTERN = """.+@.+\..+"""
