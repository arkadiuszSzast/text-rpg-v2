package com.szastarek.text.rpg.event.store

import com.eventstore.dbclient.PersistentSubscription

interface EventStoreSubscriber {
	suspend fun subscribe(): PersistentSubscription
}
