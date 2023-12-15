package com.szastarek.text.rpg.event.store

import com.eventstore.dbclient.ExpectedRevision
import kotlin.reflect.KClass

interface EventStoreWriteClient {
	suspend fun <T : DomainEvent> appendToStream(
		event: T,
		clazz: KClass<T>,
		causedBy: EventMetadata? = null,
	): EventStoreWriteResult

	suspend fun <T : DomainEvent> appendToStream(
		event: T,
		clazz: KClass<T>,
		expectedRevision: ExpectedRevision,
		causedBy: EventMetadata? = null,
	): EventStoreWriteResult
}

suspend inline fun <reified T : DomainEvent> EventStoreWriteClient.appendToStream(
	event: T,
	causedBy: EventMetadata? = null,
) = appendToStream(event, T::class, causedBy)

suspend inline fun <reified T : DomainEvent> EventStoreWriteClient.appendToStream(
	event: T,
	expectedRevision: ExpectedRevision,
	causedBy: EventMetadata? = null,
) = appendToStream(event, T::class, expectedRevision, causedBy)
