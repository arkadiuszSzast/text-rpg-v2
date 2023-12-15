package com.szastarek.text.rpg.shared.validate

import arrow.core.Either
import arrow.core.Either.Left
import arrow.core.Either.Right

fun <T> Either<ValidationErrors, T>.getOrThrow(): T {
	return when (this) {
		is Left -> throw ValidationException(this.value)
		is Right -> this.value
	}
}
