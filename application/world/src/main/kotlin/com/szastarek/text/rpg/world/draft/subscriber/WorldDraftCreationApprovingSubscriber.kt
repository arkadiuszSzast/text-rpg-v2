package com.szastarek.text.rpg.world.draft.subscriber

import com.eventstore.dbclient.NackAction
import com.eventstore.dbclient.PersistentSubscription
import com.szastarek.text.rpg.event.store.ConsumerGroup
import com.szastarek.text.rpg.event.store.EventMetadata
import com.szastarek.text.rpg.event.store.EventStoreSubscribeClient
import com.szastarek.text.rpg.event.store.EventStoreSubscriber
import com.szastarek.text.rpg.event.store.EventStoreWriteClient
import com.szastarek.text.rpg.event.store.PersistentSubscriptionOptions
import com.szastarek.text.rpg.event.store.appendToStream
import com.szastarek.text.rpg.event.store.revision
import com.szastarek.text.rpg.world.draft.WorldDraftAggregate
import com.szastarek.text.rpg.world.draft.WorldDraftListingRepository
import com.szastarek.text.rpg.world.draft.event.WorldDraftCreationRequestedEvent
import com.szastarek.text.rpg.world.draft.event.WorldDraftEvent
import io.ktor.server.application.Application
import kotlinx.coroutines.launch
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream

fun Application.startWorldDraftCreationApprovingSubscriber(subscriber: WorldDraftCreationApprovingSubscriber) =
	launch {
		subscriber.subscribe()
	}

class WorldDraftCreationApprovingSubscriber(
	private val eventStoreSubscribeClient: EventStoreSubscribeClient,
	private val eventStoreWriteClient: EventStoreWriteClient,
	private val worldDraftListingRepository: WorldDraftListingRepository,
	private val json: Json,
) : EventStoreSubscriber {
	@OptIn(ExperimentalSerializationApi::class)
	override suspend fun subscribe(): PersistentSubscription {
		return eventStoreSubscribeClient.subscribePersistentByEventType(
			WorldDraftCreationRequestedEvent.eventType,
			ConsumerGroup("world-draft-creation-approver"),
			PersistentSubscriptionOptions().bufferSize(1),
		) { subscription, resolvedEvent ->
			val event =
				json.decodeFromStream<WorldDraftCreationRequestedEvent>(resolvedEvent.event.eventData.inputStream())
			val metadata = json.decodeFromStream<EventMetadata>(resolvedEvent.event.userMetadata.inputStream())

			val existingDrafts = worldDraftListingRepository.findAllByAccountId(event.creatorAccountContext.accountId)
			existingDrafts.onLeft {
				subscription.nack(
					NackAction.Retry,
					"List of drafts of account ${event.ownerId} is not up to date.",
					resolvedEvent,
				)
			}.map { drafts ->
				WorldDraftAggregate.create(event, drafts.drafts).fold(
					{ eventStoreWriteClient.appendToStream<WorldDraftEvent>(it, it.revision(), metadata) },
					{ eventStoreWriteClient.appendToStream<WorldDraftEvent>(it, it.revision(), metadata) },
				)
			}
		}
	}
}
