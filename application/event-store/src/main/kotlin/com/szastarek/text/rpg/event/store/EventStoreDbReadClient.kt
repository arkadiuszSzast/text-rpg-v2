package com.szastarek.text.rpg.event.store

import com.eventstore.dbclient.EventStoreDBClient
import com.eventstore.dbclient.ReadResult
import com.eventstore.dbclient.ReadStreamOptions
import com.szastarek.text.rpg.monitoring.execute
import io.opentelemetry.api.OpenTelemetry
import kotlinx.coroutines.future.await

class EventStoreDbReadClient(
    private val eventStoreDBClient: EventStoreDBClient,
    private val openTelemetry: OpenTelemetry
) : EventStoreReadClient {
    override suspend fun readStream(streamName: StreamName, options: ReadStreamOptions): ReadResult {
        val tracer = openTelemetry.getTracer("event-store-db")

        return tracer.spanBuilder("event_store read ${streamName.value}")
            .startSpan()
            .execute {
                eventStoreDBClient.readStream(streamName.value, options).await()
            }
    }
}
