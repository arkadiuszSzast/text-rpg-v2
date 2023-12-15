package com.szastarek.text.rpg.event.store

import com.szastarek.text.rpg.shared.UUIDSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.litote.kmongo.Id
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

@Serializable
data class EventMetadata(
	@SerialName("\$eventId") val eventId: EventId,
	@SerialName("\$correlationId") val correlationId: CorrelationId,
	@SerialName("\$causationId") val causationId: CausationId,
	val aggregateId: AggregateId,
	val eventCategory: EventCategory,
	val eventType: EventType,
	val tracingData: Map<String, String> = emptyMap(),
)

val EventMetadata.streamName: StreamName
	get() = StreamName("${eventCategory.value}-${aggregateId.value}")

@JvmInline
@Serializable
value class AggregateId(val value: String)

fun Id<*>.asAggregateId(): AggregateId = AggregateId(this.toString())

@JvmInline
@Serializable
value class EventId(
	@Serializable(with = UUIDSerializer::class) val value: UUID,
) {
	companion object {
		fun generate(): EventId = EventId(UUID.randomUUID())
	}
}

@JvmInline
@Serializable
value class CorrelationId(
	@Serializable(with = UUIDSerializer::class) val value: UUID,
)

@JvmInline
@Serializable
value class CausationId(
	@Serializable(with = UUIDSerializer::class) val value: UUID,
)

data class EventMetadataBuilder(
	var aggregateId: AggregateId,
	var eventCategory: EventCategory,
	var eventType: EventType,
	var eventId: EventId? = null,
	var correlationId: CorrelationId? = null,
	var causationId: CausationId? = null,
	var tracingData: ConcurrentHashMap<String, String> = ConcurrentHashMap(),
) {
	companion object {
		fun fromPrototype(eventMetadata: EventMetadata) =
			EventMetadataBuilder(
				eventId = eventMetadata.eventId,
				correlationId = eventMetadata.correlationId,
				causationId = eventMetadata.causationId,
				aggregateId = eventMetadata.aggregateId,
				eventCategory = eventMetadata.eventCategory,
				eventType = eventMetadata.eventType,
				tracingData = ConcurrentHashMap(eventMetadata.tracingData),
			)
	}

	fun withTracingProperty(
		key: String,
		value: String,
	): EventMetadataBuilder {
		tracingData[key] = value
		return this
	}

	fun causedBy(causedBy: EventMetadata): EventMetadataBuilder {
		this.correlationId = causedBy.correlationId
		this.causationId = CausationId(causedBy.eventId.value)
		return this
	}

	fun optionalCausedBy(causedBy: EventMetadata?): EventMetadataBuilder {
		return causedBy?.let { causedBy(it) } ?: this
	}

	fun build(): EventMetadata {
		val eventId = this.eventId ?: EventId.generate()
		val correlationId = this.correlationId ?: CorrelationId(eventId.value)
		val causationId = this.causationId ?: CausationId(eventId.value)
		return EventMetadata(eventId, correlationId, causationId, aggregateId, eventCategory, eventType, tracingData)
	}
}
