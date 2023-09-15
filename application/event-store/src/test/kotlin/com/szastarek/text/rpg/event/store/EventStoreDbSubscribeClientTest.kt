package com.szastarek.text.rpg.event.store

import com.eventstore.dbclient.EventStoreDBClient
import com.eventstore.dbclient.EventStoreDBConnectionString
import com.eventstore.dbclient.EventStoreDBPersistentSubscriptionsClient
import com.szastarek.text.rpg.event.store.utils.EmailSent
import com.szastarek.text.rpg.event.store.utils.EventStoreContainer
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.opentelemetry.api.trace.propagation.W3CTraceContextPropagator
import io.opentelemetry.context.propagation.ContextPropagators
import io.opentelemetry.sdk.OpenTelemetrySdk
import io.opentelemetry.sdk.testing.exporter.InMemorySpanExporter
import io.opentelemetry.sdk.trace.SdkTracerProvider
import io.opentelemetry.sdk.trace.export.SimpleSpanProcessor
import java.time.Duration
import java.util.concurrent.atomic.AtomicInteger
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import org.awaitility.kotlin.await
import org.litote.kmongo.id.serialization.IdKotlinXSerializationModule
import org.litote.kmongo.newId

@OptIn(ExperimentalSerializationApi::class)
class EventStoreDbSubscribeClientTest : DescribeSpec() {

    private val spanExporter = InMemorySpanExporter.create()

    private val tracerProvider = SdkTracerProvider.builder()
        .addSpanProcessor(SimpleSpanProcessor.create(spanExporter))
        .build()

    private val openTelemetry = OpenTelemetrySdk.builder()
        .setTracerProvider(tracerProvider)
        .setPropagators(ContextPropagators.create(W3CTraceContextPropagator.getInstance()))
        .build()

    private val json = Json { serializersModule = IdKotlinXSerializationModule }

    private lateinit var eventStoreDbClient: EventStoreDBClient

    private lateinit var subscriptionClient: EventStoreDBPersistentSubscriptionsClient

    private lateinit var eventStoreDbWriteClient: EventStoreWriteClient

    private lateinit var eventStoreDbSubscribeClient: EventStoreSubscribeClient

    init {

        describe("EventStoreDbSubscribeClientTest") {

            beforeTest {
                spanExporter.reset()
                EventStoreContainer.restart()
                subscriptionClient = EventStoreDBPersistentSubscriptionsClient.create(
                    EventStoreDBConnectionString.parseOrThrow(
                        EventStoreContainer.connectionString
                    )
                )
                eventStoreDbClient = EventStoreDBClient.create(
                    EventStoreDBConnectionString.parseOrThrow(
                        EventStoreContainer.connectionString
                    )
                )
                eventStoreDbSubscribeClient = EventStoreDbSubscribeClient(subscriptionClient, json, openTelemetry)
                eventStoreDbWriteClient = EventStoreDbWriteClient(eventStoreDbClient, json, openTelemetry)
            }

            it("should subscribe by event category and process event") {
                //arrange
                val event = EmailSent(newId())
                val eventMetadata = event.getMetadata()
                val processedEventsCount = AtomicInteger(0)

                eventStoreDbWriteClient.appendToStream(event)

                //act
                eventStoreDbSubscribeClient.subscribePersistentByEventCategory(
                    eventMetadata.eventCategory,
                    ConsumerGroup("test")
                ) { _, resolvedEvent ->
                    val result = json.decodeFromStream<EmailSent>(resolvedEvent.event.eventData.inputStream())
                    if (result == event) {
                        processedEventsCount.incrementAndGet()
                    }
                }

                //assert
                await.atMost(Duration.ofMillis(500)).untilAsserted {
                    processedEventsCount.get() shouldBe 1
                }
            }

            it("should subscribe by event type and process event") {
                //arrange
                val event = EmailSent(newId())
                val eventMetadata = event.getMetadata()
                val processedEventsCount = AtomicInteger(0)

                eventStoreDbWriteClient.appendToStream(event)

                //act
                eventStoreDbSubscribeClient.subscribePersistentByEventType(
                    eventMetadata.eventType,
                    ConsumerGroup("test")
                ) { _, resolvedEvent ->
                    val result = json.decodeFromStream<EmailSent>(resolvedEvent.event.eventData.inputStream())
                    if (result == event) {
                        processedEventsCount.incrementAndGet()
                    }
                }

                //assert
                await.atMost(Duration.ofMillis(500)).untilAsserted {
                    processedEventsCount.get() shouldBe 1
                }
            }

            it("should retry processing event when exception is thrown") {
                //arrange
                val event = EmailSent(newId())
                val eventMetadata = event.getMetadata()
                val processedEventsCount = AtomicInteger(0)
                val attemptsCount = AtomicInteger(0)

                eventStoreDbWriteClient.appendToStream(event)

                //act
                eventStoreDbSubscribeClient.subscribePersistentByEventType(
                    eventMetadata.eventType,
                    ConsumerGroup("test"),
                    PersistentSubscriptionOptions().maxRetries(3)
                ) { _, resolvedEvent ->
                    if(attemptsCount.getAndIncrement() < 3) {
                        throw RuntimeException("test")
                    }
                    val result = json.decodeFromStream<EmailSent>(resolvedEvent.event.eventData.inputStream())
                    if (result == event) {
                        processedEventsCount.incrementAndGet()
                    }
                }

                //assert
                await.atMost(Duration.ofMillis(500)).untilAsserted {
                    processedEventsCount.get() shouldBe 1
                    attemptsCount.get() shouldBe 4
                }
            }

            it("should process event in child span") {
                //arrange
                val event = EmailSent(newId())
                val eventMetadata = event.getMetadata()
                val processedEventsCount = AtomicInteger(0)

                eventStoreDbWriteClient.appendToStream(event)
                val appenderSpan = spanExporter.finishedSpanItems.single()

                //act
                eventStoreDbSubscribeClient.subscribePersistentByEventType(
                    eventMetadata.eventType,
                    ConsumerGroup("test")
                ) { _, resolvedEvent ->
                    val result = json.decodeFromStream<EmailSent>(resolvedEvent.event.eventData.inputStream())
                    if (result == event) {
                        processedEventsCount.incrementAndGet()
                    }
                }

                await.atMost(Duration.ofMillis(500)).untilAsserted {
                    processedEventsCount.get() shouldBe 1
                }

                //assert
                val subscriberSpan = spanExporter.finishedSpanItems.last()
                subscriberSpan.parentSpanId shouldBe appenderSpan.spanContext.spanId
            }
        }
    }
}
