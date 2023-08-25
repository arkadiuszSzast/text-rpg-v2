package com.szastarek.text.rpg.monitoring.plugin

import com.szastarek.text.rpg.documentation.config.MonitoringProperties
import com.szastarek.text.rpg.shared.config.ConfigKey
import com.szastarek.text.rpg.shared.config.getBooleanProperty
import com.szastarek.text.rpg.shared.config.getStringProperty
import io.ktor.server.application.Application
import org.koin.core.context.loadKoinModules
import org.koin.dsl.module

internal val monitoringModule = module {
    single {
        MonitoringProperties(
            enabled = getBooleanProperty(ConfigKey("monitoring.enabled")),
            otelMetricsUrl = getStringProperty(ConfigKey("monitoring.otel.metrics.url")),
        )
    }
}

internal fun Application.configureKoin() {
    loadKoinModules(monitoringModule)
}
