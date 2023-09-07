package com.szastarek.text.rpg.event.store

import com.eventstore.dbclient.AppendToStreamOptions
import com.eventstore.dbclient.EventDataBuilder
import com.eventstore.dbclient.EventStoreDBClient
import com.eventstore.dbclient.ExpectedRevision
import com.eventstore.dbclient.WriteResult
import com.szastarek.text.rpg.monitoring.execute
import com.szastarek.text.rpg.shared.Version
import com.szastarek.text.rpg.shared.Versioned
import io.opentelemetry.api.OpenTelemetry
import io.opentelemetry.context.Context
import io.opentelemetry.context.propagation.TextMapSetter
import kotlin.reflect.KClass
import kotlin.reflect.full.createType
import kotlinx.coroutines.future.await
import kotlinx.serialization.json.Json
import kotlinx.serialization.serializer

class EventStoreDbWriteClient(
    private val eventStoreDBClient: EventStoreDBClient,
    private val json: Json,
    private val openTelemetry: OpenTelemetry
) : EventStoreWriteClient {
    override suspend fun <T : DomainEvent> appendToStream(
        event: T,
        clazz: KClass<T>,
        causedBy: EventMetadata?
    ): WriteResult {
        return append(event, clazz, causedBy)
    }

    override suspend fun <T : DomainEvent> appendToStream(
        event: T,
        clazz: KClass<T>,
        expectedRevision: ExpectedRevision,
        causedBy: EventMetadata?
    ): WriteResult {
        return append(event, clazz, causedBy, AppendToStreamOptions.get().expectedRevision(expectedRevision))
    }

    private suspend fun <T : DomainEvent> append(
        event: T,
        clazz: KClass<T>,
        causedBy: EventMetadata?,
        appendToStreamOptions: AppendToStreamOptions = AppendToStreamOptions.get()
    ): WriteResult {
        val tracer = openTelemetry.getTracer("event-store-db")
        val eventMetadata = event.getMetadata(causedBy)

        return tracer.spanBuilder("event_store publish ${eventMetadata.eventType.value}")
            .startSpan()
            .execute {
                val eventMetadataWithTrace = EventMetadataBuilder.fromPrototype(eventMetadata).apply {
                    openTelemetry.propagators.textMapPropagator.inject(
                        Context.current(),
                        this,
                        EventStoreDbTracingContextSetter
                    )
                }.build()

                val eventBytes = json.encodeToString(serializer(clazz.createType()), event).encodeToByteArray()
                val metadataBytes = json.encodeToString(EventMetadata.serializer(), eventMetadataWithTrace).encodeToByteArray()

                val eventData = EventDataBuilder.json(eventMetadata.eventId.value, eventMetadata.eventType.value, eventBytes)
                    .metadataAsBytes(metadataBytes)
                    .build()

                eventStoreDBClient.appendToStream(
                    eventMetadataWithTrace.streamName.value,
                    appendToStreamOptions,
                    eventData
                ).await()
            }
    }
}

object EventStoreDbTracingContextSetter : TextMapSetter<EventMetadataBuilder> {
    override fun set(carrier: EventMetadataBuilder?, key: String, value: String) {
        if (carrier == null) {
            return
        }
        carrier.withTracingProperty(key, value)
    }
}

fun Versioned.revision(): ExpectedRevision {
    return when (this.version) {
        Version.initial -> ExpectedRevision.noStream()
        else -> ExpectedRevision.expectedRevision(this.version.value - 1)
    }
}
