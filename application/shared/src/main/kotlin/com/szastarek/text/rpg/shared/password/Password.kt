package com.szastarek.text.rpg.shared.password

import arrow.core.nel
import arrow.core.raise.either
import arrow.core.raise.ensure
import com.szastarek.text.rpg.shared.validate.ValidationError
import kotlinx.serialization.Serializable
import org.mindrot.jbcrypt.BCrypt

@JvmInline
@Serializable
value class RawPassword private constructor(val value: String) {

    companion object {
        operator fun invoke(
            value: String,
        ) = either {
            ensure(value.length >= 8) { ValidationError(".password", "validation.password_too_short").nel() }

            RawPassword(value)
        }

        fun createWithoutValidation(value: String) = RawPassword(value)
    }

    fun hashpw() = HashedPassword(BCrypt.hashpw(value, BCrypt.gensalt()))


    override fun toString(): String {
        return "RawPassword(value='*masked*')"
    }
}

@JvmInline
@Serializable
value class HashedPassword(val value: String) {
    fun matches(rawPassword: RawPassword) = BCrypt.checkpw(rawPassword.value, this.value)

    override fun toString(): String {
        return "HashedPassword(value='*masked*')"
    }
}
