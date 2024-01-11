package com.szastarek.text.rpg.event.store

import arrow.core.Option
import arrow.core.raise.option
import com.eventstore.dbclient.EventStoreDBProjectionManagementClient
import com.eventstore.dbclient.GetProjectionStateOptions
import com.fasterxml.jackson.databind.JsonNode
import com.szastarek.text.rpg.monitoring.execute
import io.github.oshai.kotlinlogging.KotlinLogging
import io.opentelemetry.api.OpenTelemetry
import kotlinx.coroutines.future.await
import kotlinx.serialization.json.Json
import kotlinx.serialization.serializer
import kotlin.reflect.KClass
import kotlin.reflect.full.createType

class EventStoreDbProjectionsClient(
	private val client: EventStoreDBProjectionManagementClient,
	private val json: Json,
	private val openTelemetry: OpenTelemetry,
) : EventStoreProjectionsClient {
	private val logger = KotlinLogging.logger {}

	override suspend fun createOrUpdateContinuous(
		name: ProjectionName,
		query: ProjectionQuery,
	) {
		val tracer = openTelemetry.getTracer("event-store-db")
		return tracer.spanBuilder("event_store create or update projection ${name.value}").startSpan().execute {
			val exists = client.list().await().any { it.name == name.value }
			if (exists) {
				logger.debug { "Projection ${name.value} already exists. Updating query." }
				client.update(
					name.value,
					query.value,
				).await()
			} else {
				logger.debug { "Creating projection ${name.value}." }
				client.create(
					name.value,
					query.value,
				).await()
			}
			client.enable(name.value)
		}
	}

	@Suppress("UNCHECKED_CAST")
	override suspend fun <T : Any> getResult(
		name: ProjectionName,
		clazz: KClass<T>,
		partition: Partition,
	): Option<T> {
		val tracer = openTelemetry.getTracer("event-store-db")
		return tracer.spanBuilder("event_store projection ${name.value} result").startSpan().execute {
			val result =
				client.getState(
					name.value,
					JsonNode::class.java,
					GetProjectionStateOptions.get().partition(partition.value),
				).await()

			option {
				ensure(!result.isEmpty)
				json.decodeFromString(serializer(clazz.createType()), result.toString()) as T
			}
		}
	}
}
