package com.szastarek.text.rpg.world.draft.subscriber

import arrow.core.nel
import com.eventstore.dbclient.EventStoreDBClient
import com.eventstore.dbclient.EventStoreDBConnectionString.parseOrThrow
import com.eventstore.dbclient.EventStoreDBPersistentSubscriptionsClient
import com.szastarek.text.rpg.acl.serializable
import com.szastarek.text.rpg.acl.worldCreatorAuthenticatedAccountContext
import com.szastarek.text.rpg.event.store.EventStoreContainer
import com.szastarek.text.rpg.event.store.EventStoreContainerFactory
import com.szastarek.text.rpg.event.store.EventStoreDbReadClient
import com.szastarek.text.rpg.event.store.EventStoreDbSubscribeClient
import com.szastarek.text.rpg.event.store.EventStoreDbWriteClient
import com.szastarek.text.rpg.event.store.EventStoreLifecycleListener
import com.szastarek.text.rpg.event.store.appendToStream
import com.szastarek.text.rpg.event.store.readStreamByEventType
import com.szastarek.text.rpg.utils.InMemoryOpenTelemetry
import com.szastarek.text.rpg.world.draft.command.WorldDraftCreationRequestError
import com.szastarek.text.rpg.world.draft.event.WorldDraftCreationApprovedEvent
import com.szastarek.text.rpg.world.draft.event.WorldDraftCreationRejectedEvent
import com.szastarek.text.rpg.world.draft.event.WorldDraftEvent
import com.szastarek.text.rpg.world.support.InMemoryWorldDraftListingRepository
import com.szastarek.text.rpg.world.support.aWorldDraftCreationRequestedEvent
import com.szastarek.text.rpg.world.support.aWorldDraftListItem
import io.kotest.common.runBlocking
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import kotlinx.serialization.json.Json
import org.awaitility.kotlin.await
import org.awaitility.kotlin.untilAsserted
import org.litote.kmongo.id.serialization.IdKotlinXSerializationModule

class WorldDraftCreationApprovingSubscriberTest : StringSpec({

	val eventStoreContainer: EventStoreContainer = EventStoreContainerFactory.spawn()
	val subscriptionClient =
		EventStoreDBPersistentSubscriptionsClient.create(parseOrThrow(eventStoreContainer.connectionString))
	val eventStoreDbClient = EventStoreDBClient.create(parseOrThrow(eventStoreContainer.connectionString))
	val openTelemetry = InMemoryOpenTelemetry()
	val json =
		Json {
			serializersModule = IdKotlinXSerializationModule
			ignoreUnknownKeys = true
		}
	val eventStoreSubscribeClient = EventStoreDbSubscribeClient(subscriptionClient, json, openTelemetry.get())
	val eventStoreWriteClient = EventStoreDbWriteClient(eventStoreDbClient, json, openTelemetry.get())
	val eventStoreReadClient = EventStoreDbReadClient(eventStoreDbClient, json, openTelemetry.get())
	val listingRepository = InMemoryWorldDraftListingRepository()

	listener(EventStoreLifecycleListener(eventStoreContainer))

	beforeTest {
		openTelemetry.reset()
		listingRepository.clear()
	}

	"should append WorldDraftCreationApprovedEvent" {
		WorldDraftCreationApprovingSubscriber(eventStoreSubscribeClient, eventStoreWriteClient, listingRepository, json)
		val accountContext = worldCreatorAuthenticatedAccountContext.serializable()
		val event = aWorldDraftCreationRequestedEvent(accountContext = accountContext)

		// act
		eventStoreWriteClient.appendToStream<WorldDraftEvent>(event)

		// assert
		await untilAsserted {
			runBlocking {
				val expectedEvent = WorldDraftCreationApprovedEvent(event.draftId, event.version.next())
				val result =
					eventStoreReadClient.readStreamByEventType<WorldDraftCreationApprovedEvent>(
						WorldDraftCreationApprovedEvent.eventType,
					).singleOrNull()

				result shouldBe expectedEvent
			}
		}
	}

	"should append WorldDraftCreationRejectedEvent" {
		// arrange
		WorldDraftCreationApprovingSubscriber(eventStoreSubscribeClient, eventStoreWriteClient, listingRepository, json)
		val accountContext = worldCreatorAuthenticatedAccountContext.serializable()
		repeat(3) { listingRepository.add(aWorldDraftListItem(owner = accountContext.accountId)) }
		val event = aWorldDraftCreationRequestedEvent(accountContext = accountContext)

		// act
		eventStoreWriteClient.appendToStream<WorldDraftEvent>(event)

		// assert
		await untilAsserted {
			runBlocking {
				val expectedEvent =
					WorldDraftCreationRejectedEvent(
						event.draftId,
						WorldDraftCreationRequestError.MaximumNumberOfDraftsReached.nel(),
						event.version.next(),
					)
				val result =
					eventStoreReadClient.readStreamByEventType<WorldDraftCreationRejectedEvent>(
						WorldDraftCreationRejectedEvent.eventType,
					).singleOrNull()

				result shouldBe expectedEvent
			}
		}
	}
})
