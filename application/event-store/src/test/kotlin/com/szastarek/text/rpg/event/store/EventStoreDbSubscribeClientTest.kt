package com.szastarek.text.rpg.event.store

import com.eventstore.dbclient.EventStoreDBClient
import com.eventstore.dbclient.EventStoreDBConnectionString
import com.eventstore.dbclient.EventStoreDBPersistentSubscriptionsClient
import com.szastarek.text.rpg.event.store.config.EventStoreProperties
import com.szastarek.text.rpg.event.store.utils.EmailSent
import com.szastarek.text.rpg.utils.InMemoryOpenTelemetry
import io.kotest.assertions.nondeterministic.eventually
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import org.litote.kmongo.id.serialization.IdKotlinXSerializationModule
import org.litote.kmongo.newId
import java.util.UUID
import java.util.concurrent.atomic.AtomicInteger
import kotlin.time.Duration.Companion.seconds

@OptIn(ExperimentalSerializationApi::class)
class EventStoreDbSubscribeClientTest : DescribeSpec() {
	private val eventStoreContainer: EventStoreContainer = EventStoreContainerFactory.spawn()
	private val eventStoreProperties = EventStoreProperties(
		eventStoreContainer.connectionString,
		false
	)

	private val openTelemetry = InMemoryOpenTelemetry()

	private val json = Json { serializersModule = IdKotlinXSerializationModule }

	private val eventStoreDbClient =
		EventStoreDBClient.create(
			EventStoreDBConnectionString.parseOrThrow(
				eventStoreContainer.connectionString,
			),
		)

	private val eventStoreDbWriteClient = EventStoreDbWriteClient(eventStoreDbClient, json, openTelemetry.get())

	private val eventStoreDbSubscribeClient = EventStoreDbSubscribeClient(eventStoreProperties, json, openTelemetry.get())

	init {

		listener(EventStoreLifecycleListener(eventStoreContainer))

		describe("EventStoreDbSubscribeClientTest") {

			beforeTest {
				openTelemetry.reset()
			}

			it("should subscribe by event category and process event") {
				// arrange
				val event = EmailSent(newId())
				val eventMetadata = event.getMetadata()
				val processedEventsCount = AtomicInteger(0)

				eventStoreDbWriteClient.appendToStream(event)

				// act
				val subscription =
					eventStoreDbSubscribeClient.subscribePersistentByEventCategory(
						eventMetadata.eventCategory,
						ConsumerGroup(UUID.randomUUID().toString()),
					) { _, resolvedEvent ->
						val result = json.decodeFromStream<EmailSent>(resolvedEvent.event.eventData.inputStream())
						if (result == event) {
							processedEventsCount.incrementAndGet()
						}
					}

				// assert
				eventually(10.seconds) {
					processedEventsCount.get() shouldBe 1
				}
				subscription.stop()
			}

			it("should subscribe by event type and process event") {
				// arrange
				val event = EmailSent(newId())
				val eventMetadata = event.getMetadata()
				val processedEventsCount = AtomicInteger(0)

				eventStoreDbWriteClient.appendToStream(event)

				// act
				val subscription =
					eventStoreDbSubscribeClient.subscribePersistentByEventType(
						eventMetadata.eventType,
						ConsumerGroup(UUID.randomUUID().toString()),
					) { _, resolvedEvent ->
						val result = json.decodeFromStream<EmailSent>(resolvedEvent.event.eventData.inputStream())
						if (result == event) {
							processedEventsCount.incrementAndGet()
						}
					}

				// assert
				eventually(10.seconds) {
					processedEventsCount.get() shouldBe 1
				}
				subscription.stop()
			}

			it("should retry processing event when exception is thrown") {
				// arrange
				val event = EmailSent(newId())
				val eventMetadata = event.getMetadata()
				val processedEventsCount = AtomicInteger(0)
				val attemptsCount = AtomicInteger(0)

				eventStoreDbWriteClient.appendToStream(event)

				// act
				val subscription =
					eventStoreDbSubscribeClient.subscribePersistentByEventType(
						eventMetadata.eventType,
						ConsumerGroup(UUID.randomUUID().toString()),
						PersistentSubscriptionOptions().maxRetries(3),
					) { _, resolvedEvent ->
						if (attemptsCount.getAndIncrement() < 3) {
							throw RuntimeException("test")
						}
						val result = json.decodeFromStream<EmailSent>(resolvedEvent.event.eventData.inputStream())
						if (result == event) {
							processedEventsCount.incrementAndGet()
						}
					}

				// assert
				eventually(10.seconds) {
					processedEventsCount.get() shouldBe 1
					attemptsCount.get() shouldBe 4
				}
				subscription.stop()
			}

			it("should process event in child span") {
				// arrange
				val event = EmailSent(newId())
				val eventMetadata = event.getMetadata()
				val processedEventsCount = AtomicInteger(0)

				eventStoreDbWriteClient.appendToStream(event)
				val appenderSpan = openTelemetry.getFinishedSpans().single()

				// act
				val subscription =
					eventStoreDbSubscribeClient.subscribePersistentByEventType(
						eventMetadata.eventType,
						ConsumerGroup(UUID.randomUUID().toString()),
					) { _, resolvedEvent ->
						val result = json.decodeFromStream<EmailSent>(resolvedEvent.event.eventData.inputStream())
						if (result == event) {
							processedEventsCount.incrementAndGet()
						}
					}

				eventually(10.seconds) {
					processedEventsCount.get() shouldBe 1
				}

				// assert
				val subscriberSpan = openTelemetry.getFinishedSpans().last()
				subscriberSpan.parentSpanId shouldBe appenderSpan.spanContext.spanId
				subscription.stop()
			}
		}
	}
}
