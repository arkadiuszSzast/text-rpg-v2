package com.szastarek.text.rpg.event.store

import com.eventstore.dbclient.EventStoreDBClient
import com.eventstore.dbclient.ReadStreamOptions
import com.eventstore.dbclient.StreamNotFoundException
import com.szastarek.text.rpg.monitoring.execute
import io.opentelemetry.api.OpenTelemetry
import kotlinx.coroutines.future.await
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import kotlinx.serialization.serializer
import kotlin.reflect.KClass
import kotlin.reflect.full.createType

@OptIn(ExperimentalSerializationApi::class)
class EventStoreDbReadClient(
	private val eventStoreDBClient: EventStoreDBClient,
	private val json: Json,
	private val openTelemetry: OpenTelemetry,
) : EventStoreReadClient {
	override suspend fun <T : DomainEvent> readStreamByEventType(
		eventType: EventType,
		clazz: KClass<T>,
		options: ReadStreamOptions,
	): List<T> {
		return readStream(StreamName("\$et-${eventType.value}"), clazz, options)
	}

	@Suppress("UNCHECKED_CAST")
	override suspend fun <T : DomainEvent> readStream(
		streamName: StreamName,
		clazz: KClass<T>,
		options: ReadStreamOptions,
	): List<T> {
		val tracer = openTelemetry.getTracer("event-store-db")

		return tracer.spanBuilder("event_store read ${streamName.value}")
			.setAttribute("db.system", "eventstore-db")
			.startSpan()
			.execute {
				val rawEvents =
					try {
						eventStoreDBClient.readStream(streamName.value, options).await().events
					} catch (ex: StreamNotFoundException) {
						emptyList()
					}

				rawEvents
					.map { json.decodeFromStream(serializer(clazz.createType()), it.event.eventData.inputStream()) as T }
			}
	}
}
