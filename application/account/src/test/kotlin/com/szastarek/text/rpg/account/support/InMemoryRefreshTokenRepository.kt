package com.szastarek.text.rpg.account.support

import arrow.core.Option
import arrow.core.toOption
import com.szastarek.text.rpg.account.RefreshToken
import com.szastarek.text.rpg.account.RefreshTokenRepository
import com.szastarek.text.rpg.shared.email.EmailAddress

class InMemoryRefreshTokenRepository : RefreshTokenRepository {

  private val db = mutableMapOf<EmailAddress, RefreshToken>()

  override suspend fun getAndDelete(accountEmail: EmailAddress): Option<RefreshToken> {
    return db.remove(accountEmail).toOption()
  }

  override suspend fun replace(accountEmail: EmailAddress, token: RefreshToken): RefreshToken {
    db[accountEmail] = token
    return token
  }

  fun clear() {
    db.clear()
  }
}
