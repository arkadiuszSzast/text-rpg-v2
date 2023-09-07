package com.szastarek.text.rpg.shared

import kotlinx.serialization.Serializable

interface Versioned {
    val version: Version
}

@JvmInline
@Serializable
value class Version(val value: Long) {
    init {
        require(value >= 0) { "Version must be greater or equal to 0" }
    }

    companion object {
        val initial = Version(0)
    }

    fun next(): Version = Version(value + 1)
}
