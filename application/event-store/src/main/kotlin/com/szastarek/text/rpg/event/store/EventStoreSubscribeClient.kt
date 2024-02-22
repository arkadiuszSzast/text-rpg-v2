package com.szastarek.text.rpg.event.store

import com.eventstore.dbclient.PersistentSubscription
import com.eventstore.dbclient.ResolvedEvent
import com.szastarek.text.rpg.event.store.config.EventStoreProperties
import io.ktor.events.EventDefinition
import io.ktor.server.application.Application
import io.ktor.server.application.ApplicationStopPreparing
import io.ktor.server.application.BaseApplicationPlugin
import io.ktor.util.AttributeKey
import io.opentelemetry.api.GlobalOpenTelemetry
import io.opentelemetry.api.OpenTelemetry
import kotlinx.serialization.json.Json

typealias PersistentEventListener = suspend (subscription: PersistentSubscription, resolvedEvent: ResolvedEvent) -> Unit

class EventStoreDbSubscriptionPluginConfiguration {
	var eventStoreProperties: EventStoreProperties? = null
	var json: Json = Json
	var openTelemetry: OpenTelemetry = GlobalOpenTelemetry.get()
}

interface EventStoreSubscribeClient {
	suspend fun subscribePersistentByEventCategory(
		eventCategory: EventCategory,
		consumerGroup: ConsumerGroup,
		options: PersistentSubscriptionOptions = PersistentSubscriptionOptions(),
		listener: PersistentEventListener,
	): PersistentSubscription

	suspend fun subscribePersistentByEventType(
		eventType: EventType,
		consumerGroup: ConsumerGroup,
		options: PersistentSubscriptionOptions = PersistentSubscriptionOptions(),
		listener: PersistentEventListener,
	): PersistentSubscription

	companion object Feature :
		BaseApplicationPlugin<Application, EventStoreDbSubscriptionPluginConfiguration, EventStoreSubscribeClient> {
		override val key: AttributeKey<EventStoreSubscribeClient> = AttributeKey("EventStoreDB")
		private val ClosedEvent = EventDefinition<Unit>()

		override fun install(
			pipeline: Application,
			configure: EventStoreDbSubscriptionPluginConfiguration.() -> Unit,
		): EventStoreSubscribeClient {
			val applicationMonitor = pipeline.environment.monitor
			val config = EventStoreDbSubscriptionPluginConfiguration().apply(configure)
			val plugin =
				EventStoreDbSubscribeClient(
					config.eventStoreProperties ?: throw IllegalStateException("EventStoreProperties cannot be null"),
					config.json,
					config.openTelemetry,
				)

			applicationMonitor.subscribe(ApplicationStopPreparing) {
				plugin.shutdown()
				it.monitor.raise(ClosedEvent, Unit)
			}
			return plugin
		}
	}
}

@JvmInline
value class ConsumerGroup(val value: String)
