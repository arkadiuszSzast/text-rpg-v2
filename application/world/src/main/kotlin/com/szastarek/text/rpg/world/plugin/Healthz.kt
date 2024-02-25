package com.szastarek.text.rpg.world.plugin

import com.eventstore.dbclient.EventStoreDBClient
import com.szastarek.text.rpg.shared.plugin.HealthzPlugin
import io.ktor.server.application.Application
import io.ktor.server.application.install
import kotlinx.coroutines.future.await

internal fun Application.configureHealthz(eventStoreDBClient: EventStoreDBClient) {
	install(HealthzPlugin) {
		healthChecks {
			check("main") { true }
			check("eventstore") { eventStoreDBClient.serverVersion.await().isPresent }
		}
		readyChecks {
			check("main") { true }
			check("eventstore") { eventStoreDBClient.serverVersion.await().isPresent }
		}
	}
}
