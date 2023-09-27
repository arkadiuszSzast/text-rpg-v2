package com.szastarek.text.rpg.mail

import com.szastarek.text.rpg.event.store.*
import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

@Serializable
sealed interface MailSendingEvent : DomainEvent {
  val mail: Mail

  companion object {
    val eventCategory: EventCategory
      get() = EventCategory("mail")
  }
}

@Transient
val MailSendingEvent.aggregateId: AggregateId
  get() = mail.id.asAggregateId()

@Serializable
data class MailSentEvent(override val mail: Mail, val sentAt: Instant) : MailSendingEvent {
  companion object {
    val eventType = EventType(MailSendingEvent.eventCategory, "sent")
  }

  override fun getMetadata(causedBy: EventMetadata?): EventMetadata {
    return EventMetadataBuilder(
      aggregateId,
      MailSendingEvent.eventCategory,
      eventType
    ).optionalCausedBy(causedBy).build()
  }
}

@Serializable
data class MailSendingErrorEvent(
  override val mail: Mail,
  val failedAt: Instant,
  val errors: List<MailSendingError>
) : MailSendingEvent {

  companion object {
    val eventType = EventType(MailSendingEvent.eventCategory, "sending-error")
  }

  override fun getMetadata(causedBy: EventMetadata?): EventMetadata {
    return EventMetadataBuilder(
      aggregateId,
      MailSendingEvent.eventCategory,
      eventType
    ).optionalCausedBy(causedBy).build()
  }
}
