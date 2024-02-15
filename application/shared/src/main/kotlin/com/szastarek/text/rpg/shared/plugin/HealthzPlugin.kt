package com.szastarek.text.rpg.shared.plugin

import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.ApplicationStarted
import io.ktor.server.application.call
import io.ktor.server.application.createApplicationPlugin
import io.ktor.server.application.hooks.MonitoringEvent
import io.ktor.server.response.respondText
import io.ktor.server.routing.get
import io.ktor.server.routing.routing
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

data class Check(val checkName: String, val check: CheckFunction)
typealias CheckFunction = suspend () -> Boolean

class HealthzPluginConfiguration {
	internal var healthChecks = linkedSetOf<Check>()
	internal var readyChecks = linkedSetOf<Check>()

	var healthCheckPath = "/health"
	var readyCheckPath = "/ready"

	var healthCheckEnabled = true
	var readyCheckEnabled = true

	var successfulCheckStatusCode = HttpStatusCode.OK
	var unsuccessfulCheckStatusCode = HttpStatusCode.InternalServerError

	fun healthChecks(init: CheckBuilder.() -> Unit) {
		healthChecks = CheckBuilder().apply(init).checks
	}

	fun readyChecks(init: CheckBuilder.() -> Unit) {
		readyChecks = CheckBuilder().apply(init).checks
	}
}

class CheckBuilder {
	val checks = linkedSetOf<Check>()

	fun check(
		name: String,
		check: CheckFunction,
	) {
		checks.add(Check(name, check))
	}
}

val HealthzPlugin =
	createApplicationPlugin(name = "Healthz", createConfiguration = ::HealthzPluginConfiguration) {
		val config = this.pluginConfig
		on(MonitoringEvent(ApplicationStarted)) { application ->
			application.routing {
				if (config.healthCheckEnabled) {
					get(config.healthCheckPath) {
						val (status, responseBody) =
							processChecks(
								checkLinkedList = config.healthChecks,
								passingStatusCode = config.successfulCheckStatusCode,
								failingStatusCode = config.unsuccessfulCheckStatusCode,
							)
						this.call.respondText(responseBody, ContentType.Application.Json, status)
					}
				}

				if (config.readyCheckEnabled) {
					get(config.readyCheckPath) {
						val (status, responseBody) =
							processChecks(
								checkLinkedList = config.readyChecks,
								passingStatusCode = config.successfulCheckStatusCode,
								failingStatusCode = config.unsuccessfulCheckStatusCode,
							)
						this.call.respondText(responseBody, ContentType.Application.Json, status)
					}
				}
			}
		}
	}

private suspend fun processChecks(
	checkLinkedList: LinkedHashSet<Check>,
	passingStatusCode: HttpStatusCode,
	failingStatusCode: HttpStatusCode,
): Pair<HttpStatusCode, String> {
	val checksWithResults = checkLinkedList.associate { Pair(it.checkName, it.check.invoke()) }
	val status =
		if (checksWithResults.containsValue(false)) {
			failingStatusCode
		} else {
			passingStatusCode
		}
	return Pair(status, Json.encodeToString(checksWithResults))
}
