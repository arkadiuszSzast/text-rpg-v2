package com.szastarek.text.rpg.account

import arrow.core.Option
import com.szastarek.text.rpg.shared.email.EmailAddress
import java.util.UUID

interface RefreshTokenRepository {
  suspend fun getAndDelete(accountEmail: EmailAddress): Option<RefreshToken>

  suspend fun replace(accountEmail: EmailAddress, token: RefreshToken): RefreshToken
}

@JvmInline
value class RefreshToken(val value: String) {
  companion object {
    fun generate() = RefreshToken(UUID.randomUUID().toString())
  }
}
