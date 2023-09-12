package com.szastarek.text.rpg.event.store.plugin

import com.eventstore.dbclient.EventStoreDBClient
import com.eventstore.dbclient.EventStoreDBConnectionString.parseOrThrow
import com.eventstore.dbclient.EventStoreDBPersistentSubscriptionsClient
import com.szastarek.text.rpg.event.store.EventStoreDbReadClient
import com.szastarek.text.rpg.event.store.EventStoreDbSubscribeClient
import com.szastarek.text.rpg.event.store.EventStoreDbWriteClient
import com.szastarek.text.rpg.event.store.EventStoreReadClient
import com.szastarek.text.rpg.event.store.EventStoreSubscribeClient
import com.szastarek.text.rpg.event.store.EventStoreWriteClient
import com.szastarek.text.rpg.event.store.config.EventStoreProperties
import com.szastarek.text.rpg.shared.config.ConfigKey
import com.szastarek.text.rpg.shared.config.getStringProperty
import io.ktor.server.application.Application
import io.opentelemetry.api.GlobalOpenTelemetry
import org.koin.core.context.GlobalContext.loadKoinModules
import org.koin.dsl.bind
import org.koin.dsl.module

internal val eventStoreModule = module {
    single { EventStoreProperties(getStringProperty(ConfigKey("eventStore.connectionString"))) }
    single { GlobalOpenTelemetry.get() }
    single { EventStoreDBClient.create(parseOrThrow(get<EventStoreProperties>().connectionString)) }
    single { EventStoreDBPersistentSubscriptionsClient.create(parseOrThrow(get<EventStoreProperties>().connectionString)) }
    single { EventStoreDbReadClient(get(), get()) } bind EventStoreReadClient::class
    single { EventStoreDbWriteClient(get(), get(), get()) } bind EventStoreWriteClient::class
    single { EventStoreDbSubscribeClient(get(), get(), get()) } bind EventStoreSubscribeClient::class
}

internal fun Application.configureKoin() {
    loadKoinModules(eventStoreModule)
}