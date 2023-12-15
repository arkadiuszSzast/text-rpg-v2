package com.szastarek.text.rpg.mail.adapter.sendgrid

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import com.szastarek.text.rpg.event.store.EventMetadata
import com.szastarek.text.rpg.event.store.EventStoreWriteClient
import com.szastarek.text.rpg.event.store.appendToStream
import com.szastarek.text.rpg.mail.Mail
import com.szastarek.text.rpg.mail.MailSender
import com.szastarek.text.rpg.mail.MailSendingError
import com.szastarek.text.rpg.mail.MailSendingErrorEvent
import com.szastarek.text.rpg.mail.MailSendingEvent
import com.szastarek.text.rpg.mail.MailSentEvent
import com.szastarek.text.rpg.monitoring.execute
import io.ktor.client.call.body
import io.ktor.http.isSuccess
import io.opentelemetry.api.OpenTelemetry
import kotlinx.datetime.Clock

class SendGridMailSender(
	private val sendGridClient: SendgridClient,
	private val writeEventStoreClient: EventStoreWriteClient,
	private val openTelemetry: OpenTelemetry,
	private val clock: Clock,
) : MailSender {
	override suspend fun send(
		mail: Mail,
		causedBy: EventMetadata?,
	): Either<List<MailSendingError>, Mail> {
		val tracer = openTelemetry.getTracer("sendgrid-client")

		return tracer.spanBuilder("send-mail")
			.startSpan()
			.execute {
				val request = mail.toSendgridSendMailRequest()

				val response = sendGridClient.sendMail(request)

				when (response.status.isSuccess()) {
					false -> {
						val errors = response.body<SendgridErrorResponse>().errors.map { MailSendingError(it.message) }
						val event = MailSendingErrorEvent(mail, clock.now(), errors)
						writeEventStoreClient.appendToStream<MailSendingEvent>(event, causedBy)
						errors.left()
					}

					true -> {
						writeEventStoreClient.appendToStream<MailSendingEvent>(MailSentEvent(mail, clock.now()), causedBy)
						mail.right()
					}
				}
			}
	}

	private fun Mail.toSendgridSendMailRequest(): SendgridSendMailRequest {
		val personalization =
			SendgridPersonalization(
				to = listOf(SendgridEmail(this.to.value)),
				subject = this.subject.value,
				dynamicTemplateData = this.variables.values,
			)

		return SendgridSendMailRequest(
			from = SendgridEmail(this.from.value),
			templateId = this.templateId.value,
			personalization = listOf(personalization),
		)
	}
}
