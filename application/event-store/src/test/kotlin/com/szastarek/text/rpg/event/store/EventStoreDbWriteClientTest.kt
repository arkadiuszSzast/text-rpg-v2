package com.szastarek.text.rpg.event.store

import com.eventstore.dbclient.EventStoreDBClient
import com.eventstore.dbclient.EventStoreDBConnectionString.parseOrThrow
import com.eventstore.dbclient.ExpectedRevision
import com.eventstore.dbclient.ReadStreamOptions
import com.eventstore.dbclient.WrongExpectedVersionException
import com.szastarek.text.rpg.event.store.utils.EventStoreContainer
import com.szastarek.text.rpg.shared.Version
import com.szastarek.text.rpg.shared.Versioned
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.opentelemetry.api.trace.propagation.W3CTraceContextPropagator
import io.opentelemetry.context.propagation.ContextPropagators
import io.opentelemetry.sdk.OpenTelemetrySdk
import io.opentelemetry.sdk.testing.exporter.InMemorySpanExporter
import io.opentelemetry.sdk.trace.SdkTracerProvider
import io.opentelemetry.sdk.trace.export.SimpleSpanProcessor
import kotlinx.coroutines.future.await
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.litote.kmongo.Id
import org.litote.kmongo.id.serialization.IdKotlinXSerializationModule
import org.litote.kmongo.newId

class EventStoreDbWriteClientTest : DescribeSpec() {

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

    private lateinit var eventStoreDbWriteClient: EventStoreWriteClient

    init {

        describe("EventStoreDbWriteClientTest") {

            beforeTest {
                EventStoreContainer.restart()
                eventStoreDbClient = EventStoreDBClient.create(parseOrThrow(EventStoreContainer.connectionString))
                eventStoreDbWriteClient = EventStoreDbWriteClient(
                    eventStoreDbClient,
                    json,
                    openTelemetry
                )
                spanExporter.reset()
            }

            it("should append event") {
                //arrange
                val event = EmailSent(newId())

                //act
                val result = eventStoreDbWriteClient.appendToStream(event)

                //assert
                result.nextExpectedRevision.toRawLong() shouldBe 0
            }

            it("should append versioned events") {
                //arrange
                val accountCreatedEvent = AccountCreated(newId(), "test")
                val accountUpdatedEvent = AccountNameUpdated(accountCreatedEvent.id, "test2", accountCreatedEvent.version.next())

                //act
                val createAccountResult = eventStoreDbWriteClient.appendToStream(accountCreatedEvent, accountCreatedEvent.revision())
                val updateAccountResult = eventStoreDbWriteClient.appendToStream(accountUpdatedEvent, accountUpdatedEvent.revision())

                //assert
                createAccountResult.nextExpectedRevision.toRawLong() shouldBe 0
                updateAccountResult.nextExpectedRevision.toRawLong() shouldBe 1
            }

            it("should throw exception when appending event with wrong expected revision") {
                //arrange
                val accountCreatedEvent = AccountCreated(newId(), "test")

                //act & assert
                shouldThrow<WrongExpectedVersionException> {
                    eventStoreDbWriteClient.appendToStream(accountCreatedEvent, ExpectedRevision.expectedRevision(1))
                }
            }

            it("should append event in new span") {
                //arrange
                val accountCreatedEvent = AccountCreated(newId(), "test")

                //act
                eventStoreDbWriteClient.appendToStream(accountCreatedEvent)

                //assert
                spanExporter.finishedSpanItems.single().name shouldBe "event_store publish account-created"
            }

            it("should append event with caused by") {
                //arrange
                val accountCreatedEvent = AccountCreated(newId(), "test")
                val accountUpdatedEvent = AccountNameUpdated(accountCreatedEvent.id, "test2", accountCreatedEvent.version.next())

                eventStoreDbWriteClient.appendToStream(accountCreatedEvent)
                val accountCreatedMetadata = eventStoreDbClient.readStream(
                    "account-${accountCreatedEvent.id}",
                    ReadStreamOptions.get()
                ).await().events.first().event.userMetadata.let { json.decodeFromString<EventMetadata>(String(it)) }

                //act
                eventStoreDbWriteClient.appendToStream(accountUpdatedEvent, accountCreatedMetadata)

                //assert
                val accountUpdatedMetadata = eventStoreDbClient.readStream(
                    "account-${accountCreatedEvent.id}",
                    ReadStreamOptions.get()
                ).await().events.last().event.userMetadata.let { json.decodeFromString<EventMetadata>(String(it)) }

                accountUpdatedMetadata.eventId shouldNotBe accountCreatedMetadata.eventId
                accountUpdatedMetadata.causationId.value shouldBe accountCreatedMetadata.eventId.value
                accountUpdatedMetadata.correlationId.value shouldBe accountCreatedMetadata.correlationId.value
            }

            it("should append span details to event metadata") {
                //arrange
                val accountCreatedEvent = AccountCreated(newId(), "test")

                //act
                eventStoreDbWriteClient.appendToStream(accountCreatedEvent)

                //assert
                val span = spanExporter.finishedSpanItems.single()
                val accountCreatedMetadata = eventStoreDbClient.readStream(
                    "account-${accountCreatedEvent.id}",
                    ReadStreamOptions.get()
                ).await().events.first().event.userMetadata.let { json.decodeFromString<EventMetadata>(String(it)) }

                accountCreatedMetadata.tracingData["traceparent"] shouldBe "00-${span.traceId}-${span.spanId}-01"
            }
        }
    }
}

@Serializable
data class Account(@Contextual val id: Id<Account>, val name: String)

@Serializable
data class AccountCreated(@Contextual val id: Id<Account>, val name: String) : DomainEvent, Versioned {
    override fun getMetadata(causedBy: EventMetadata?): EventMetadata {
        return EventMetadataBuilder(
            id.asAggregateId(),
            EventCategory("account"),
            EventType("account-created")
        ).optionalCausedBy(causedBy).build()
    }

    override val version: Version = Version.initial
}

@Serializable
data class AccountNameUpdated(@Contextual val id: Id<Account>, val name: String, override val version: Version) :
    DomainEvent, Versioned {
    override fun getMetadata(causedBy: EventMetadata?): EventMetadata {
        return EventMetadataBuilder(
            id.asAggregateId(),
            EventCategory("account"),
            EventType("account-name-updated")
        ).optionalCausedBy(causedBy).build()
    }
}

@Serializable
data class Email(@Contextual val id: Id<Email>)

@Serializable
data class EmailSent(@Contextual val id: Id<Email>) : DomainEvent {
    override fun getMetadata(causedBy: EventMetadata?): EventMetadata {
        return EventMetadataBuilder(
            id.asAggregateId(),
            EventCategory("email"),
            EventType("email-sent")
        ).optionalCausedBy(causedBy).build()
    }
}
