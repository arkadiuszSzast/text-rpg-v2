package com.szastarek.text.rpg.utils

import io.opentelemetry.api.OpenTelemetry
import io.opentelemetry.api.trace.propagation.W3CTraceContextPropagator
import io.opentelemetry.context.propagation.ContextPropagators
import io.opentelemetry.sdk.OpenTelemetrySdk
import io.opentelemetry.sdk.testing.exporter.InMemorySpanExporter
import io.opentelemetry.sdk.trace.SdkTracerProvider
import io.opentelemetry.sdk.trace.export.SimpleSpanProcessor

class InMemoryOpenTelemetry {
	private val spanExporter = InMemorySpanExporter.create()

	private val tracerProvider =
		SdkTracerProvider.builder()
			.addSpanProcessor(SimpleSpanProcessor.create(spanExporter))
			.build()

	private val openTelemetry =
		OpenTelemetrySdk.builder()
			.setTracerProvider(tracerProvider)
			.setPropagators(ContextPropagators.create(W3CTraceContextPropagator.getInstance()))
			.build()

	fun getFinishedSpans() = spanExporter.finishedSpanItems

	fun get(): OpenTelemetry = openTelemetry

	fun reset() = spanExporter.reset()
}
