package com.szastarek.text.rpg.event.store

import arrow.core.Option
import kotlin.reflect.KClass

interface EventStoreProjectionsClient {
	suspend fun createOrUpdateContinuous(
		name: ProjectionName,
		query: ProjectionQuery,
	)

	suspend fun <T : Any> getResult(
		name: ProjectionName,
		clazz: KClass<T>,
		partition: Partition,
	): Option<T>
}

suspend inline fun <reified T : Any> EventStoreProjectionsClient.getResult(
	name: ProjectionName,
	partition: Partition,
) = getResult(name, T::class, partition)
