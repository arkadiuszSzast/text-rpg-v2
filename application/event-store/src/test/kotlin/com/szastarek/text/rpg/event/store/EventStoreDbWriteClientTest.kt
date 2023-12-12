package com.szastarek.text.rpg.event.store

import com.eventstore.dbclient.EventStoreDBClient
import com.eventstore.dbclient.EventStoreDBConnectionString.parseOrThrow
import com.eventstore.dbclient.ExpectedRevision
import com.eventstore.dbclient.ReadStreamOptions
import com.szastarek.text.rpg.event.store.utils.AccountCreated
import com.szastarek.text.rpg.event.store.utils.AccountNameUpdated
import com.szastarek.text.rpg.event.store.utils.EmailSent
import com.szastarek.text.rpg.utils.InMemoryOpenTelemetry
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import kotlinx.coroutines.future.await
import kotlinx.serialization.json.Json
import org.litote.kmongo.id.serialization.IdKotlinXSerializationModule
import org.litote.kmongo.newId

class EventStoreDbWriteClientTest : DescribeSpec() {

  private val openTelemetry = InMemoryOpenTelemetry()

  private val json = Json { serializersModule = IdKotlinXSerializationModule }

  private val eventStoreDbClient = EventStoreDBClient.create(parseOrThrow(EventStoreContainer.connectionString))

  private val eventStoreDbWriteClient = EventStoreDbWriteClient(
    eventStoreDbClient,
    json,
    openTelemetry.get()
  )

  init {

    describe("EventStoreDbWriteClientTest") {

      threads = 1

      beforeTest {
        EventStoreContainer.restart()
        openTelemetry.reset()
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
        val accountUpdatedEvent =
          AccountNameUpdated(accountCreatedEvent.id, "test2", accountCreatedEvent.version.next())

        //act
        val createAccountResult =
          eventStoreDbWriteClient.appendToStream(accountCreatedEvent, accountCreatedEvent.revision())
        val updateAccountResult =
          eventStoreDbWriteClient.appendToStream(accountUpdatedEvent, accountUpdatedEvent.revision())

        //assert
        createAccountResult.nextExpectedRevision.toRawLong() shouldBe 0
        updateAccountResult.nextExpectedRevision.toRawLong() shouldBe 1
      }

      it("should throw exception when appending event with wrong expected revision") {
        //arrange
        val accountCreatedEvent = AccountCreated(newId(), "test")

        //act & assert
        shouldThrow<InvalidExpectedRevisionException> {
          eventStoreDbWriteClient.appendToStream(accountCreatedEvent, ExpectedRevision.expectedRevision(1))
        }
      }

      it("should append event in new span") {
        //arrange
        val accountCreatedEvent = AccountCreated(newId(), "test")

        //act
        eventStoreDbWriteClient.appendToStream(accountCreatedEvent)

        //assert
        openTelemetry.getFinishedSpans().single().name shouldBe "event_store publish account-created"
      }

      it("should append event with caused by") {
        //arrange
        val accountCreatedEvent = AccountCreated(newId(), "test")
        val accountUpdatedEvent =
          AccountNameUpdated(accountCreatedEvent.id, "test2", accountCreatedEvent.version.next())

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
        val span = openTelemetry.getFinishedSpans().single()
        val accountCreatedMetadata = eventStoreDbClient.readStream(
          "account-${accountCreatedEvent.id}",
          ReadStreamOptions.get()
        ).await().events.first().event.userMetadata.let { json.decodeFromString<EventMetadata>(String(it)) }

        accountCreatedMetadata.tracingData["traceparent"] shouldBe "00-${span.traceId}-${span.spanId}-01"
      }
    }
  }
}
