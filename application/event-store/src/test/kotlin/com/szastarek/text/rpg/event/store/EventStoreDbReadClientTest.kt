package com.szastarek.text.rpg.event.store

import com.eventstore.dbclient.EventDataBuilder
import com.eventstore.dbclient.EventStoreDBClient
import com.eventstore.dbclient.EventStoreDBConnectionString.parseOrThrow
import com.szastarek.text.rpg.event.store.utils.EmailSent
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.opentelemetry.api.trace.propagation.W3CTraceContextPropagator
import io.opentelemetry.context.propagation.ContextPropagators
import io.opentelemetry.sdk.OpenTelemetrySdk
import io.opentelemetry.sdk.testing.exporter.InMemorySpanExporter
import io.opentelemetry.sdk.trace.SdkTracerProvider
import io.opentelemetry.sdk.trace.export.SimpleSpanProcessor
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.litote.kmongo.id.serialization.IdKotlinXSerializationModule
import org.litote.kmongo.newId

class EventStoreDbReadClientTest : DescribeSpec() {

    private val spanExporter = InMemorySpanExporter.create()

    private val tracerProvider = SdkTracerProvider.builder()
        .addSpanProcessor(SimpleSpanProcessor.create(spanExporter))
        .build()

    private val openTelemetry = OpenTelemetrySdk.builder()
        .setTracerProvider(tracerProvider)
        .setPropagators(ContextPropagators.create(W3CTraceContextPropagator.getInstance()))
        .build()

    private val json = Json { serializersModule = IdKotlinXSerializationModule }

    private val eventStoreDbClient = EventStoreDBClient.create(parseOrThrow(EventStoreContainer.connectionString))

    private val eventStoreDbReadClient = EventStoreDbReadClient(
        eventStoreDbClient,
        json,
        openTelemetry
    )

    init {

        describe("EventStoreDbReadClientTest") {

            beforeTest {
                EventStoreContainer.restart()
                spanExporter.reset()
            }

            it("should read stream in new span") {
                //arrange
                val emailSentEvent = EmailSent(newId())
                val eventMetadata = emailSentEvent.getMetadata()
                val eventData = EventDataBuilder.json(
                    eventMetadata.eventId.value,
                    eventMetadata.eventType.value,
                    json.encodeToString(emailSentEvent).encodeToByteArray()
                ).build()
                eventStoreDbClient.appendToStream(eventMetadata.streamName.value, eventData)

                //act
                val result = eventStoreDbReadClient.readStream<EmailSent>(eventMetadata.streamName)

                //assert
                result shouldHaveSize 1
                spanExporter.finishedSpanItems.single().name shouldBe "event_store read ${eventMetadata.streamName.value}"
            }

            it("should return empty list when stream does not exist") {
                //arrange & act & assert
                eventStoreDbReadClient.readStream<EmailSent>(StreamName("not-existing")).shouldBeEmpty()
            }
        }
    }
}
