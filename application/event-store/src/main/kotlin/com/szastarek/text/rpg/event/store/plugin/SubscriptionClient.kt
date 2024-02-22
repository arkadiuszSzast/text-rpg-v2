package com.szastarek.text.rpg.event.store.plugin

import com.szastarek.text.rpg.event.store.EventStoreSubscribeClient
import com.szastarek.text.rpg.event.store.config.EventStoreProperties
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.opentelemetry.api.OpenTelemetry
import kotlinx.serialization.json.Json

internal fun Application.eventStoreDb(
	eventStoreProperties: EventStoreProperties,
	json: Json,
	openTelemetry: OpenTelemetry,
): EventStoreSubscribeClient {
	return install(EventStoreSubscribeClient) {
		this.eventStoreProperties = eventStoreProperties
		this.json = json
		this.openTelemetry = openTelemetry
	}
}
