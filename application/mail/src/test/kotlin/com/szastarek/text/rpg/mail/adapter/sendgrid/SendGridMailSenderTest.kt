package com.szastarek.text.rpg.mail.adapter.sendgrid

import arrow.core.nel
import com.szastarek.text.rpg.event.store.*
import com.szastarek.text.rpg.mail.*
import com.szastarek.text.rpg.mail.config.SendGridProperties
import com.szastarek.text.rpg.shared.anEmail
import com.szastarek.text.rpg.utils.FixedClock
import io.kotest.assertions.arrow.core.shouldBeLeft
import io.kotest.assertions.arrow.core.shouldBeRight
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.should
import io.kotest.matchers.shouldBe
import io.ktor.client.engine.mock.*
import io.ktor.http.*
import io.opentelemetry.api.trace.propagation.W3CTraceContextPropagator
import io.opentelemetry.context.propagation.ContextPropagators
import io.opentelemetry.sdk.OpenTelemetrySdk
import io.opentelemetry.sdk.testing.exporter.InMemorySpanExporter
import io.opentelemetry.sdk.trace.SdkTracerProvider
import io.opentelemetry.sdk.trace.export.SimpleSpanProcessor
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import org.litote.kmongo.newId
import java.util.UUID

@OptIn(ExperimentalSerializationApi::class)
class SendGridMailSenderTest : DescribeSpec() {

  private val clock = FixedClock()

  private val json = Json

  private val spanExporter = InMemorySpanExporter.create()

  private val tracerProvider = SdkTracerProvider.builder()
    .addSpanProcessor(SimpleSpanProcessor.create(spanExporter))
    .build()

  private val openTelemetry = OpenTelemetrySdk.builder()
    .setTracerProvider(tracerProvider)
    .setPropagators(ContextPropagators.create(W3CTraceContextPropagator.getInstance()))
    .build()

  private val sendGridProperties = SendGridProperties("sendgrid-api-key", "sendgrid.test.com")

  private val mockEngine = MockEngine { request ->
    val decodedRequest = json.decodeFromStream<SendgridSendMailRequest>(request.body.toByteArray().inputStream())
    when {
      decodedRequest.subject == "invalid-mail" -> respondBadRequest()
      else -> respondOk()
    }
  }

  private val sendgridClient = SendgridClient(mockEngine, sendGridProperties)

  private val eventStore = InMemoryEventStore()

  private val sendgridMailSender = SendGridMailSender(sendgridClient, eventStore, openTelemetry, clock)

  init {

    describe("SendGridMailSenderTest") {

      beforeTest {
        spanExporter.reset()
        eventStore.clear()
      }

      it("should send email in new span") {
        //arrange
        val mail = Mail(
          id = newId(),
          subject = MailSubject("test-mail-subject"),
          from = anEmail(),
          to = anEmail(),
          templateId = MailTemplateId("test-template"),
          variables = MailVariables(emptyMap())
        )
        val expectedEvents = listOf(MailSentEvent(mail, clock.now()))

        //act
        val result = sendgridMailSender.send(mail)

        //assert
        result.shouldBeRight(mail)
        eventStore.readStreamByEventType(MailSentEvent.eventType, MailSentEvent::class) shouldBe expectedEvents

        spanExporter.finishedSpanItems.single().name shouldBe "send-mail"
      }

      it("should append event about error") {
        //arrange
        val mail = Mail(
          id = newId(),
          subject = MailSubject("invalid-mail"),
          from = anEmail(),
          to = anEmail(),
          templateId = MailTemplateId("test-template"),
          variables = MailVariables(emptyMap())
        )
        val expectedEvents = listOf(MailSendingErrorEvent(mail, clock.now(), MailSendingError.Unknown.nel()))

        //act
        val result = sendgridMailSender.send(mail)

        //assert
        result.shouldBeLeft(MailSendingError.Unknown.nel())
        eventStore.readStreamByEventType(MailSendingErrorEvent.eventType, MailSendingErrorEvent::class) shouldBe expectedEvents
      }

      it("should add caused by to the event") {
        //arrange
        val mail = Mail(
          id = newId(),
          subject = MailSubject("test-mail-subject"),
          from = anEmail(),
          to = anEmail(),
          templateId = MailTemplateId("test-template"),
          variables = MailVariables(emptyMap())
        )
        val causedBy = EventMetadataBuilder(
          AggregateId(UUID.randomUUID().toString()),
          EventCategory("causing-category"),
          EventType("causing-type")
        ).build()
        val expectedEvent = MailSentEvent(mail, clock.now())

        //act
        val result = sendgridMailSender.send(mail, causedBy)

        //assert
        result.shouldBeRight(mail)
        eventStore.getMetadata(expectedEvent).shouldNotBeNull() should {
          it.correlationId shouldBe causedBy.correlationId
          it.causationId shouldBe CausationId(causedBy.eventId.value)
        }
      }
    }
  }
}
