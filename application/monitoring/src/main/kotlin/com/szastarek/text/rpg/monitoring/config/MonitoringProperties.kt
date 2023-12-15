package com.szastarek.text.rpg.monitoring.config

data class MonitoringProperties(
	val enabled: Boolean,
	val otelMetricsUrl: String,
)
