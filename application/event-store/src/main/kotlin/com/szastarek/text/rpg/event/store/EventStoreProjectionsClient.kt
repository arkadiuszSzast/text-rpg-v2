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
		config: GetProjectionResultConfig = GetProjectionResultConfig(),
	): ProjectionResult<out T>
}

suspend inline fun <reified T : Any> EventStoreProjectionsClient.getResult(
	name: ProjectionName,
	partition: Partition,
	config: GetProjectionResultConfig = GetProjectionResultConfig(),
) = getResult(name, T::class, partition, config)

data class GetProjectionResultConfig(
	val maxRetries: Long = 3,
	val retryDelayMs: Long = 20,
)

sealed interface ProjectionResult<T>

data class ProjectionOutdatedResult<T>(val data: Option<T>) : ProjectionResult<T>

data class ProjectionUpToDateResult<T>(val data: T) : ProjectionResult<T>

data object ProjectionResultNotFound : ProjectionResult<Nothing>
