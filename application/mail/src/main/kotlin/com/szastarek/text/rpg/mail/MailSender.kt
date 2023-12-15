package com.szastarek.text.rpg.mail

import arrow.core.Either
import com.szastarek.text.rpg.event.store.EventMetadata
import kotlinx.serialization.Serializable

interface MailSender {
	suspend fun send(
		mail: Mail,
		causedBy: EventMetadata? = null,
	): Either<List<MailSendingError>, Mail>
}

@JvmInline
@Serializable
value class MailSendingError(val message: String)
