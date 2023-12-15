package com.szastarek.text.rpg.event.store

import com.eventstore.dbclient.ExpectedRevision
import com.eventstore.dbclient.Position
import com.eventstore.dbclient.ReadStreamOptions
import kotlin.reflect.KClass

@Suppress("UNCHECKED_CAST")
class InMemoryEventStore : EventStoreReadClient, EventStoreWriteClient {
	private val db = mutableListOf<Event>()

	override suspend fun <T : DomainEvent> readStream(
		streamName: StreamName,
		clazz: KClass<T>,
		options: ReadStreamOptions,
	): List<T> {
		return db.filter { it.metadata.streamName == streamName }.map { it.data as T }
	}

	override suspend fun <T : DomainEvent> readStreamByEventType(
		eventType: EventType,
		clazz: KClass<T>,
		options: ReadStreamOptions,
	): List<T> {
		return db.filter { it.metadata.eventType == eventType }.map { it.data as T }
	}

	override suspend fun <T : DomainEvent> appendToStream(
		event: T,
		clazz: KClass<T>,
		expectedRevision: ExpectedRevision,
		causedBy: EventMetadata?,
	): EventStoreWriteResult {
		val metadata = event.getMetadata(causedBy)
		val countMatchingStreamName = db.filter { it.metadata.streamName == metadata.streamName }.size
		val actualRevision =
			if (countMatchingStreamName == 0) {
				ExpectedRevision.noStream()
			} else {
				ExpectedRevision.expectedRevision((countMatchingStreamName - 1).toLong())
			}
		if (expectedRevision != actualRevision) {
			throw InvalidExpectedRevisionException(metadata.streamName.value, expectedRevision, actualRevision)
		}
		return appendToStream(event, clazz, causedBy)
	}

	override suspend fun <T : DomainEvent> appendToStream(
		event: T,
		clazz: KClass<T>,
		causedBy: EventMetadata?,
	): EventStoreWriteResult {
		val metadata = event.getMetadata(causedBy)
		db.add(Event(event, metadata))
		val countMatchingStreamName = db.filter { it.metadata.streamName == metadata.streamName }.size
		val nextExpectedRevision =
			if (countMatchingStreamName == 0) {
				ExpectedRevision.noStream()
			} else {
				ExpectedRevision.expectedRevision((countMatchingStreamName - 1).toLong())
			}
		return EventStoreWriteResult(Position(db.size.toLong(), db.size.toLong()), nextExpectedRevision)
	}

	fun clear() = db.clear()

	fun getMetadata(event: DomainEvent): EventMetadata? = db.find { it.data == event }?.metadata
}

private data class Event(val data: DomainEvent, val metadata: EventMetadata)
