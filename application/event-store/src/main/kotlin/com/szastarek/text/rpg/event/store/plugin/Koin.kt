package com.szastarek.text.rpg.event.store.plugin

import com.eventstore.dbclient.EventStoreDBClient
import com.eventstore.dbclient.EventStoreDBConnectionString.parseOrThrow
import com.eventstore.dbclient.EventStoreDBPersistentSubscriptionsClient
import com.eventstore.dbclient.EventStoreDBProjectionManagementClient
import com.szastarek.text.rpg.event.store.EventStoreDbProjectionsClient
import com.szastarek.text.rpg.event.store.EventStoreDbReadClient
import com.szastarek.text.rpg.event.store.EventStoreDbWriteClient
import com.szastarek.text.rpg.event.store.EventStoreProjectionsClient
import com.szastarek.text.rpg.event.store.EventStoreReadClient
import com.szastarek.text.rpg.event.store.EventStoreWriteClient
import com.szastarek.text.rpg.event.store.config.EventStoreProperties
import com.szastarek.text.rpg.shared.config.ConfigKey
import com.szastarek.text.rpg.shared.config.getBooleanProperty
import com.szastarek.text.rpg.shared.config.getStringProperty
import io.ktor.server.application.Application
import io.opentelemetry.api.GlobalOpenTelemetry
import org.koin.core.context.GlobalContext.loadKoinModules
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module
import org.koin.ktor.ext.get

internal val eventStoreModule =
	module {
		single {
			EventStoreProperties(
				getStringProperty(ConfigKey("eventStore.connectionString")),
				getBooleanProperty(ConfigKey("eventStore.reSubscribeOnDrop")),
			)
		}
		single { GlobalOpenTelemetry.get() }
		single { EventStoreDBClient.create(parseOrThrow(get<EventStoreProperties>().connectionString)) }
		single { EventStoreDBPersistentSubscriptionsClient.create(parseOrThrow(get<EventStoreProperties>().connectionString)) }
		single { EventStoreDBProjectionManagementClient.create(parseOrThrow(get<EventStoreProperties>().connectionString)) }
		singleOf(::EventStoreDbReadClient) bind EventStoreReadClient::class
		singleOf(::EventStoreDbWriteClient) bind EventStoreWriteClient::class
		singleOf(::EventStoreDbProjectionsClient) bind EventStoreProjectionsClient::class
	}

internal fun Application.configureKoin() {
	loadKoinModules(eventStoreModule)
	loadKoinModules(module { single { this@configureKoin.eventStoreDb(get(), get(), get()) } })
}
