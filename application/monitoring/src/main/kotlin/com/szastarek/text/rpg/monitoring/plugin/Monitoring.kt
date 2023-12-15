package com.szastarek.text.rpg.monitoring.plugin

import com.szastarek.text.rpg.monitoring.config.MonitoringProperties
import com.szastarek.text.rpg.shared.plugin.installIfNotRegistered
import io.ktor.server.application.Application
import io.ktor.server.metrics.micrometer.MicrometerMetrics
import io.ktor.server.plugins.callloging.CallLogging
import io.ktor.server.request.path
import io.micrometer.core.instrument.Clock
import io.micrometer.registry.otlp.OtlpConfig
import io.micrometer.registry.otlp.OtlpMeterRegistry
import io.opentelemetry.api.OpenTelemetry
import io.opentelemetry.instrumentation.ktor.v2_0.server.KtorServerTracing
import org.slf4j.event.Level

internal fun Application.configureMonitoring(
	monitoringProperties: MonitoringProperties,
	openTelemetry: OpenTelemetry,
) {
	if (monitoringProperties.enabled) {
		val otlpConfig =
			OtlpConfig {
				when (it) {
					"otlp.url" -> monitoringProperties.otelMetricsUrl
					else -> null
				}
			}
		installIfNotRegistered(KtorServerTracing) {
			setOpenTelemetry(openTelemetry)
		}
		installIfNotRegistered(MicrometerMetrics) {
			registry = OtlpMeterRegistry(otlpConfig, Clock.SYSTEM)
		}
		installIfNotRegistered(CallLogging) {
			level = Level.INFO
			filter { call -> call.request.path().startsWith("/") }
		}
	}
}
