package com.szastarek.text.rpg.mail.adapter.sendgrid

import arrow.core.Either
import arrow.core.Nel
import arrow.core.left
import arrow.core.right
import arrow.core.nel
import com.szastarek.text.rpg.event.store.EventMetadata
import com.szastarek.text.rpg.event.store.EventStoreWriteClient
import com.szastarek.text.rpg.event.store.appendToStream
import com.szastarek.text.rpg.mail.*
import com.szastarek.text.rpg.monitoring.execute
import io.ktor.http.*
import io.opentelemetry.api.OpenTelemetry
import kotlinx.datetime.Clock

class SendGridMailSender(
  private val sendGridClient: SendgridClient,
  private val writeEventStoreClient: EventStoreWriteClient,
  private val openTelemetry: OpenTelemetry,
  private val clock: Clock
) : MailSender {
  override suspend fun send(mail: Mail, causedBy: EventMetadata?): Either<Nel<MailSendingError>, Mail> {
    val tracer = openTelemetry.getTracer("sendgrid-client")

    return tracer.spanBuilder("send-mail")
      .startSpan()
      .execute {
        val request = mail.toSendgridSendMailRequest()

        val response = sendGridClient.sendMail(request)

        when (response.status.isSuccess()) {
          false -> {
            val event = MailSendingErrorEvent(mail, clock.now(), MailSendingError.Unknown.nel())
            writeEventStoreClient.appendToStream<MailSendingEvent>(event, causedBy)
            MailSendingError.Unknown.nel().left()
          }

          true -> {
            writeEventStoreClient.appendToStream<MailSendingEvent>(MailSentEvent(mail, clock.now()), causedBy)
            mail.right()
          }
        }
      }
  }

  private fun Mail.toSendgridSendMailRequest(): SendgridSendMailRequest {
    val personalization = SendgridPersonalization(
      to = listOf(SendgridEmail(this.to.value)),
      dynamicTemplateData = this.variables.variables
    )

    return SendgridSendMailRequest(
      from = SendgridEmail(this.from.value),
      subject = this.subject.value,
      templateId = this.templateId.value,
      personalizations = listOf(personalization)
    )
  }

//  private fun Mail.toSendgridMail(): SendGridMail {
//    val mail = this
//    val personalization = Personalization().apply {
//      addTo(Email(mail.to.value))
//      addDynamicTemplateData("subject", mail.subject.value)
//      mail.variables.variables.forEach { addDynamicTemplateData(it.key, it.value) }
//    }
//
//    return SendGridMail().apply {
//      from = Email(mail.from.value)
//      templateId = mail.templateId.value
//      addPersonalization(personalization)
//    }
//  }

}