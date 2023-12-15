package com.szastarek.text.rpg.account.subscriber

import com.szastarek.text.rpg.account.activation.AccountActivationUrlProvider
import com.szastarek.text.rpg.account.adapter.mail.ActivationAccountMailVariables
import com.szastarek.text.rpg.account.config.ActivateAccountMailProperties
import com.szastarek.text.rpg.account.event.AccountCreatedEvent
import com.szastarek.text.rpg.event.store.ConsumerGroup
import com.szastarek.text.rpg.event.store.EventMetadata
import com.szastarek.text.rpg.event.store.EventStoreSubscribeClient
import com.szastarek.text.rpg.mail.Mail
import com.szastarek.text.rpg.mail.MailSender
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import org.litote.kmongo.newId
import kotlin.coroutines.CoroutineContext

@OptIn(ExperimentalSerializationApi::class)
class ActivationMailSenderSubscriber(
	private val eventStoreSubscribeClient: EventStoreSubscribeClient,
	private val mailSender: MailSender,
	private val mailTemplateProperties: ActivateAccountMailProperties,
	private val accountActivationUrlProvider: AccountActivationUrlProvider,
	private val json: Json,
) : CoroutineScope {
	override val coroutineContext: CoroutineContext
		get() = SupervisorJob()

	init {
		subscribe().start()
	}

	private fun subscribe() =
		launch(coroutineContext) {
			eventStoreSubscribeClient.subscribePersistentByEventType(
				AccountCreatedEvent.eventType,
				ConsumerGroup("activation-mail-sender"),
			) { _, resolvedEvent ->
				val accountCreatedEvent = json.decodeFromStream<AccountCreatedEvent>(resolvedEvent.event.eventData.inputStream())
				val metadata = json.decodeFromStream<EventMetadata>(resolvedEvent.event.userMetadata.inputStream())

				val activationUrl = accountActivationUrlProvider.provide(accountCreatedEvent.emailAddress)
				val mailVariables = ActivationAccountMailVariables(activationUrl).toMailVariables()
				val mail =
					Mail(
						id = newId(),
						subject = mailTemplateProperties.subject,
						from = mailTemplateProperties.sender,
						to = accountCreatedEvent.emailAddress,
						templateId = mailTemplateProperties.templateId,
						variables = mailVariables,
					)

				mailSender.send(mail, metadata)
			}
		}
}
