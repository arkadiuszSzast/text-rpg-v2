package com.szastarek.text.rpg.account

import arrow.core.Option
import com.szastarek.text.rpg.shared.email.EmailAddress

interface AccountAggregateRepository {
  suspend fun findByEmail(emailAddress: EmailAddress): Option<AccountAggregate>
}
