package com.szastarek.text.rpg.account.adapter.event.store

import arrow.core.Option
import arrow.core.toNonEmptyListOrNone
import com.szastarek.text.rpg.account.AccountAggregate
import com.szastarek.text.rpg.account.AccountAggregateBuilder
import com.szastarek.text.rpg.account.AccountAggregateRepository
import com.szastarek.text.rpg.account.event.AccountEvent
import com.szastarek.text.rpg.event.store.EventStoreReadClient
import com.szastarek.text.rpg.event.store.readStream
import com.szastarek.text.rpg.shared.email.EmailAddress
import com.szastarek.text.rpg.shared.validate.getOrThrow

class AccountAggregateEventStoreRepository(
	private val eventStoreReadClient: EventStoreReadClient,
) : AccountAggregateRepository {
	override suspend fun findByEmail(emailAddress: EmailAddress): Option<AccountAggregate> {
		val events = eventStoreReadClient.readStream<AccountEvent>(AccountEvent.aggregateStreamName(emailAddress))
		return events.toNonEmptyListOrNone()
			.map { AccountAggregateBuilder().apply(it).getOrThrow() }
	}
}
