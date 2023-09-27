package com.szastarek.text.rpg.mail

import arrow.core.Either
import arrow.core.Nel
import com.szastarek.text.rpg.event.store.EventMetadata

interface MailSender {
  suspend fun send(mail: Mail, causedBy: EventMetadata? = null): Either<Nel<MailSendingError>, Mail>
}

enum class MailSendingError {
  Unknown
}
