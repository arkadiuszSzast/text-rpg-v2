package com.szastarek.text.rpg.mail

import arrow.core.Either
import arrow.core.Nel
import arrow.core.right
import com.szastarek.text.rpg.event.store.EventMetadata

class RecordingMailSender : MailSender {

  private val sentMails: MutableList<Mail> = mutableListOf()

  override suspend fun send(
    mail: Mail,
    causedBy: EventMetadata?
  ): Either<Nel<MailSendingError>, Mail> {
    sentMails.add(mail)
    return mail.right()
  }

  fun hasBeenSent(predicate: (Mail) -> Boolean): Boolean {
    return sentMails.any { predicate(it) }
  }

  fun getAll() = sentMails.toList()

  fun clear() = sentMails.clear()
}
