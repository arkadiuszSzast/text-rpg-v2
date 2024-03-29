package com.szastarek.text.rpg.account.subscriber

import com.eventstore.dbclient.EventStoreDBClient
import com.eventstore.dbclient.EventStoreDBConnectionString
import com.eventstore.dbclient.EventStoreDBPersistentSubscriptionsClient
import com.szastarek.text.rpg.account.activation.AccountActivationUrlProvider
import com.szastarek.text.rpg.account.config.AccountActivationProperties
import com.szastarek.text.rpg.account.config.ActivateAccountMailProperties
import com.szastarek.text.rpg.account.event.AccountEvent
import com.szastarek.text.rpg.account.support.anAccountCreatedEvent
import com.szastarek.text.rpg.event.store.EventStoreContainer
import com.szastarek.text.rpg.event.store.EventStoreContainerFactory
import com.szastarek.text.rpg.event.store.EventStoreDbSubscribeClient
import com.szastarek.text.rpg.event.store.EventStoreDbWriteClient
import com.szastarek.text.rpg.event.store.EventStoreLifecycleListener
import com.szastarek.text.rpg.event.store.appendToStream
import com.szastarek.text.rpg.event.store.config.EventStoreProperties
import com.szastarek.text.rpg.mail.MailSubject
import com.szastarek.text.rpg.mail.MailTemplateId
import com.szastarek.text.rpg.mail.RecordingMailSender
import com.szastarek.text.rpg.security.JwtIssuer
import com.szastarek.text.rpg.security.JwtProperties
import com.szastarek.text.rpg.security.JwtSecret
import com.szastarek.text.rpg.shared.anEmail
import com.szastarek.text.rpg.utils.FixedClock
import com.szastarek.text.rpg.utils.InMemoryOpenTelemetry
import io.kotest.assertions.nondeterministic.eventually
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.booleans.shouldBeTrue
import io.ktor.http.Url
import kotlinx.serialization.json.Json
import org.litote.kmongo.id.serialization.IdKotlinXSerializationModule
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

class ActivationMailSenderSubscriberTest : DescribeSpec() {
	private val eventStoreContainer: EventStoreContainer = EventStoreContainerFactory.spawn()

	private val clock = FixedClock()
	private val openTelemetry = InMemoryOpenTelemetry()
	private val json =
		Json {
			serializersModule = IdKotlinXSerializationModule
			ignoreUnknownKeys = true
		}
	private val subscriptionClient =
		EventStoreDBPersistentSubscriptionsClient.create(
			EventStoreDBConnectionString.parseOrThrow(
				eventStoreContainer.connectionString,
			),
		)
	private val eventStoreProperties = EventStoreProperties(eventStoreContainer.connectionString, false)
	private val eventStoreSubscribeClient = EventStoreDbSubscribeClient(eventStoreProperties, json, openTelemetry.get())
	private val mailSender = RecordingMailSender()
	private val mailProperties =
		ActivateAccountMailProperties(
			MailTemplateId("test-template"),
			anEmail("test-sender@mail.com"),
			MailSubject("test-subject"),
		)
	private val accountActivationProperties =
		AccountActivationProperties(
			activateAccountUrl = Url("http://test-host:3000/account/activate"),
			jwtConfig =
				JwtProperties(
					JwtSecret("activate-account-jwt-test-secret"),
					JwtIssuer("activate-account-jwt-test-issuer"),
					3600000.milliseconds,
				),
		)
	private val eventStoreDbClient =
		EventStoreDBClient.create(
			EventStoreDBConnectionString.parseOrThrow(
				eventStoreContainer.connectionString,
			),
		)
	private val eventStoreWriteClient = EventStoreDbWriteClient(eventStoreDbClient, json, openTelemetry.get())
	private val accountActivationUrlProvider = AccountActivationUrlProvider(accountActivationProperties, clock)

	init {

		listener(EventStoreLifecycleListener(eventStoreContainer))

		describe("ActivationMailSenderSubscriberTest") {

			beforeTest {
				openTelemetry.reset()
			}

			it("should send mail on account created event") {
				// arrange
				ActivationMailSenderSubscriber(
					eventStoreSubscribeClient,
					mailSender,
					mailProperties,
					accountActivationUrlProvider,
					json,
				)
				val event = anAccountCreatedEvent()

				// act
				eventStoreWriteClient.appendToStream<AccountEvent>(event)

				// assert
				eventually(10.seconds) {
					mailSender.hasBeenSent {
						it.to == event.emailAddress && it.subject == mailProperties.subject
					}.shouldBeTrue()
				}
			}
		}
	}
}
