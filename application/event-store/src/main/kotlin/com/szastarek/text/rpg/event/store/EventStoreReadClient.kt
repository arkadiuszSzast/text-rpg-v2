package com.szastarek.text.rpg.event.store

import com.eventstore.dbclient.ReadStreamOptions
import kotlin.reflect.KClass

interface EventStoreReadClient {
  suspend fun <T : DomainEvent> readStream(
    streamName: StreamName,
    clazz: KClass<T>,
    options: ReadStreamOptions = ReadStreamOptions.get().resolveLinkTos()
  ): List<T>

  suspend fun <T : DomainEvent> readStreamByEventType(
    eventType: EventType,
    clazz: KClass<T>,
    options: ReadStreamOptions = ReadStreamOptions.get().resolveLinkTos()
  ): List<T>
}

suspend inline fun <reified T : DomainEvent> EventStoreReadClient.readStream(
  streamName: StreamName,
  options: ReadStreamOptions = ReadStreamOptions.get().resolveLinkTos()
) = readStream(streamName, T::class, options)

suspend inline fun <reified T : DomainEvent> EventStoreReadClient.readStreamByEventType(
  eventType: EventType,
  options: ReadStreamOptions = ReadStreamOptions.get().resolveLinkTos()
) = readStreamByEventType(eventType, T::class, options)
