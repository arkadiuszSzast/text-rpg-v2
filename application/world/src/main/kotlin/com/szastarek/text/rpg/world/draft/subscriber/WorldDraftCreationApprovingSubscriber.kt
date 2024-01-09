package com.szastarek.text.rpg.world.draft.subscriber

import com.szastarek.text.rpg.event.store.ConsumerGroup
import com.szastarek.text.rpg.event.store.EventMetadata
import com.szastarek.text.rpg.event.store.EventStoreSubscribeClient
import com.szastarek.text.rpg.event.store.EventStoreWriteClient
import com.szastarek.text.rpg.event.store.appendToStream
import com.szastarek.text.rpg.event.store.revision
import com.szastarek.text.rpg.world.draft.WorldDraftAggregate
import com.szastarek.text.rpg.world.draft.WorldDraftListingRepository
import com.szastarek.text.rpg.world.draft.event.WorldDraftCreationRequestedEvent
import com.szastarek.text.rpg.world.draft.event.WorldDraftEvent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import kotlin.coroutines.CoroutineContext

class WorldDraftCreationApprovingSubscriber(
	private val eventStoreSubscribeClient: EventStoreSubscribeClient,
	private val eventStoreWriteClient: EventStoreWriteClient,
	private val worldDraftListingRepository: WorldDraftListingRepository,
	private val json: Json,
) : CoroutineScope {
	override val coroutineContext: CoroutineContext
		get() = SupervisorJob()

	init {
		subscribe().start()
	}

	@OptIn(ExperimentalSerializationApi::class)
	private fun subscribe() =
		launch(coroutineContext) {
			eventStoreSubscribeClient.subscribePersistentByEventType(
				WorldDraftCreationRequestedEvent.eventType,
				ConsumerGroup("world-draft-creation-approver"),
			) { _, resolvedEvent ->
				val event = json.decodeFromStream<WorldDraftCreationRequestedEvent>(resolvedEvent.event.eventData.inputStream())
				val metadata = json.decodeFromStream<EventMetadata>(resolvedEvent.event.userMetadata.inputStream())

				val existingDrafts =
					worldDraftListingRepository.findAllByAccountId(event.creatorAccountContext.accountId)

				WorldDraftAggregate.create(event, existingDrafts.drafts).fold(
					{ eventStoreWriteClient.appendToStream<WorldDraftEvent>(it, it.revision(), metadata) },
					{ eventStoreWriteClient.appendToStream<WorldDraftEvent>(it, it.revision(), metadata) },
				)
			}
		}
}
