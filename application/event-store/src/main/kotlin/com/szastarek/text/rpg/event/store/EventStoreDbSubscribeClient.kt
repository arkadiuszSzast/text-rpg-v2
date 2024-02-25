package com.szastarek.text.rpg.event.store

import com.eventstore.dbclient.EventStoreDBConnectionString.parseOrThrow
import com.eventstore.dbclient.EventStoreDBPersistentSubscriptionsClient
import com.eventstore.dbclient.NackAction
import com.eventstore.dbclient.PersistentSubscription
import com.eventstore.dbclient.PersistentSubscriptionListener
import com.eventstore.dbclient.ResolvedEvent
import com.szastarek.text.rpg.event.store.config.EventStoreProperties
import com.szastarek.text.rpg.monitoring.execute
import io.github.oshai.kotlinlogging.KotlinLogging
import io.opentelemetry.api.OpenTelemetry
import io.opentelemetry.api.trace.SpanKind
import io.opentelemetry.context.Context
import io.opentelemetry.context.propagation.TextMapGetter
import kotlinx.coroutines.CompletableJob
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.ExecutorCoroutineDispatcher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.future.await
import kotlinx.coroutines.launch
import kotlinx.coroutines.newSingleThreadContext
import kotlinx.serialization.json.Json
import java.util.concurrent.atomic.AtomicInteger
import kotlin.coroutines.CoroutineContext

class EventStoreDbSubscribeClient(
    private val config: EventStoreProperties,
    private val json: Json,
    private val openTelemetry: OpenTelemetry,
) : EventStoreSubscribeClient, CoroutineScope {
    private val logger = KotlinLogging.logger {}

    private val parent: CompletableJob = Job()
    override val coroutineContext: CoroutineContext
        get() = parent

    private val client = EventStoreDBPersistentSubscriptionsClient.create(parseOrThrow(config.connectionString))

    override suspend fun subscribePersistentByEventCategory(
        eventCategory: EventCategory,
        consumerGroup: ConsumerGroup,
        options: PersistentSubscriptionOptions,
        listener: PersistentEventListener,
    ) = subscribeToPersistentStream(
        StreamName("\$ce-${eventCategory.value}"),
        consumerGroup,
        options,
        tracingPersistentListener(listener, consumerGroup),
    )

    override suspend fun subscribePersistentByEventType(
        eventType: EventType,
        consumerGroup: ConsumerGroup,
        options: PersistentSubscriptionOptions,
        listener: PersistentEventListener,
    ) = subscribeToPersistentStream(
        StreamName("\$et-${eventType.value}"),
        consumerGroup,
        options,
        tracingPersistentListener(listener, consumerGroup),
    )

    private suspend fun subscribeToPersistentStream(
        streamName: StreamName,
        customerGroup: ConsumerGroup,
        options: PersistentSubscriptionOptions,
        listener: PersistentEventListener,
    ): PersistentSubscription =
        subscriptionContext.let { context ->
            val consumerGroupExists =
                client.getInfoToStream(
                    streamName.value,
                    customerGroup.value,
                ).await().isPresent
            if (!consumerGroupExists && options.autoCreateStreamGroup) {
                logger.debug {
                    "Stream group $customerGroup not found. AutoCreateStreamGroup is ON. Trying to create the group."
                }
                client.createToStream(
                    streamName.value,
                    customerGroup.value,
                    options.createPersistentSubscriptionToStreamOptions,
                ).await()
                logger.debug { "Stream group $customerGroup created." }
            }

            client.subscribeToStream(
                streamName.value,
                customerGroup.value,
                options.subscriptionOptions,
                object : PersistentSubscriptionListener() {
                    override fun onEvent(
                        subscription: PersistentSubscription,
                        retryCount: Int,
                        event: ResolvedEvent,
                    ) {
                        launch(context) {
                            runCatching {
                                listener(subscription, event)

                                if (options.autoAcknowledge) {
                                    subscription.ack(event)
                                }
                            }.onFailure { error ->
                                val eventId = event.originalEvent.eventId
                                if (retryCount < options.maxRetries) {
                                    logger.error(error) {
                                        "Error when processing event[$eventId]. Retry attempt [${retryCount + 1}/${options.maxRetries}]"
                                    }
                                    subscription.nack(
                                        NackAction.Retry,
                                        "exception_${error::class.simpleName}",
                                        event,
                                    )
                                } else {
                                    logger.error(error) {
                                        "Error when processing event[$eventId]. Going to ${options.nackAction.name} event"
                                    }
                                    subscription.nack(
                                        options.nackAction,
                                        "exception_${error::class.simpleName}",
                                        event,
                                    )
                                }
                            }
                        }
                    }

                    override fun onCancelled(
                        subscription: PersistentSubscription?,
                        throwable: Throwable?,
                    ) {
                        launch(context) {
                            logger.error(throwable) { "Error on persisted subscription [${subscription?.subscriptionId}]" }
                            if (config.reSubscribeOnDrop) {
                                subscribeToPersistentStream(streamName, customerGroup, options, listener)
                            }
                        }
                    }
                },
            ).await()
        }

    private suspend fun tracingPersistentListener(
        listener: PersistentEventListener,
        consumerGroup: ConsumerGroup,
    ): PersistentEventListener {
        return { subscription, event ->
            val tracingData =
                json.runCatching {
                    decodeFromString<EventMetadata>(event.event.userMetadata.toString(Charsets.UTF_8))
                }.onFailure { logger.error(it) { "Error when decoding tracing data" } }.getOrNull()
            val textMapPropagator = openTelemetry.propagators.textMapPropagator
            val extractedContext = textMapPropagator.extract(Context.current(), tracingData, EventStoreDbGetter)
            extractedContext.makeCurrent().use {
                openTelemetry.getTracer("persistent-event-listener")
                    .spanBuilder(consumerGroup.value)
                    .setAttribute("db.system", "eventstore-db")
                    .setAttribute("eventType", event.event.eventType)
                    .setSpanKind(SpanKind.SERVER)
                    .startSpan()
                    .execute {
                        listener(subscription, event)
                    }
            }
        }
    }

    fun shutdown(): Boolean {
        return parent.complete().also { client.shutdown().get() }
    }

    private val subscriptionContextCounter = AtomicInteger(0)

    @OptIn(ExperimentalCoroutinesApi::class, DelicateCoroutinesApi::class)
    private val subscriptionContext: ExecutorCoroutineDispatcher
        get() =
            newSingleThreadContext("EventStoreDB-subscription-context-${subscriptionContextCounter.incrementAndGet()}")
}

private object EventStoreDbGetter : TextMapGetter<EventMetadata> {
    override fun keys(carrier: EventMetadata): MutableIterable<String> {
        return carrier.tracingData.keys.toMutableList()
    }

    override fun get(
        carrier: EventMetadata?,
        key: String,
    ): String? {
        if (carrier == null) {
            return null
        }

        return carrier.tracingData[key]
    }
}
