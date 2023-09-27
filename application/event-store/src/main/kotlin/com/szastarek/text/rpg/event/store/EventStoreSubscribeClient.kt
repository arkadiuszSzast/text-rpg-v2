package com.szastarek.text.rpg.event.store

import com.eventstore.dbclient.PersistentSubscription
import com.eventstore.dbclient.ResolvedEvent

typealias PersistentEventListener = suspend (subscription: PersistentSubscription, resolvedEvent: ResolvedEvent) -> Unit

interface EventStoreSubscribeClient {

    suspend fun subscribePersistentByEventCategory(
        eventCategory: EventCategory,
        consumerGroup: ConsumerGroup,
        options: PersistentSubscriptionOptions = PersistentSubscriptionOptions(),
        listener: PersistentEventListener
    ): PersistentSubscription

    suspend fun subscribePersistentByEventType(
        eventType: EventType,
        consumerGroup: ConsumerGroup,
        options: PersistentSubscriptionOptions = PersistentSubscriptionOptions(),
        listener: PersistentEventListener
    ): PersistentSubscription
}

@JvmInline
value class ConsumerGroup(val value: String)
