package com.szastarek.text.rpg.mail.adapter.sendgrid

import com.szastarek.text.rpg.event.store.AggregateId
import com.szastarek.text.rpg.event.store.CausationId
import com.szastarek.text.rpg.event.store.EventCategory
import com.szastarek.text.rpg.event.store.EventMetadataBuilder
import com.szastarek.text.rpg.event.store.EventType
import com.szastarek.text.rpg.event.store.InMemoryEventStore
import com.szastarek.text.rpg.mail.Mail
import com.szastarek.text.rpg.mail.MailSendingError
import com.szastarek.text.rpg.mail.MailSendingErrorEvent
import com.szastarek.text.rpg.mail.MailSentEvent
import com.szastarek.text.rpg.mail.MailSubject
import com.szastarek.text.rpg.mail.MailTemplateId
import com.szastarek.text.rpg.mail.MailVariables
import com.szastarek.text.rpg.mail.config.SendGridProperties
import com.szastarek.text.rpg.shared.anEmail
import com.szastarek.text.rpg.utils.FixedClock
import com.szastarek.text.rpg.utils.InMemoryOpenTelemetry
import io.kotest.assertions.arrow.core.shouldBeLeft
import io.kotest.assertions.arrow.core.shouldBeRight
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.should
import io.kotest.matchers.shouldBe
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.engine.mock.respondOk
import io.ktor.client.engine.mock.toByteArray
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.append
import io.ktor.http.headers
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import org.litote.kmongo.newId
import java.util.UUID
import kotlinx.serialization.encodeToString

@OptIn(ExperimentalSerializationApi::class)
class SendGridMailSenderTest : DescribeSpec() {

  private val clock = FixedClock()

  private val json = Json

  private val openTelemetry = InMemoryOpenTelemetry()

  private val sendGridProperties = SendGridProperties("sendgrid-api-key", "sendgrid.test.com")

  private val mockEngine = MockEngine { request ->
    val decodedRequest = json.decodeFromStream<SendgridSendMailRequest>(request.body.toByteArray().inputStream())
    when {
      decodedRequest.getSubject() == "invalid-mail" -> respond(
        json.encodeToString(SendgridErrorResponse(listOf(SendgridError("subject", "Invalid subject")))),
        HttpStatusCode.BadRequest,
        headers { append(HttpHeaders.ContentType, ContentType.Application.Json) }
      )
      else -> respondOk()
    }
  }

  private val sendgridClient = SendgridClient(mockEngine, sendGridProperties)

  private val eventStore = InMemoryEventStore()

  private val sendgridMailSender = SendGridMailSender(sendgridClient, eventStore, openTelemetry.get(), clock)

  init {

    describe("SendGridMailSenderTest") {

      beforeTest {
        openTelemetry.reset()
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

        openTelemetry.getFinishedSpans().single().name shouldBe "send-mail"
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
        val expectedEvents = listOf(MailSendingErrorEvent(mail, clock.now(), listOf(MailSendingError("Invalid subject"))))

        //act
        val result = sendgridMailSender.send(mail)

        //assert
        result.shouldBeLeft(listOf(MailSendingError("Invalid subject")))
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

  private fun SendgridSendMailRequest.getSubject() = this.personalization.first().dynamicTemplateData["subject"]!!
}
