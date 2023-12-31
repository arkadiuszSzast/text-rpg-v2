package com.szastarek.text.rpg.mediator.plugin

import com.szastarek.text.rpg.mediator.TracingPipelineBehavior
import com.trendyol.kediatr.koin.KediatRKoin
import io.ktor.server.application.Application
import io.opentelemetry.api.GlobalOpenTelemetry
import org.koin.core.context.loadKoinModules
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

internal val mediatorModule =
	module {
		single { GlobalOpenTelemetry.get() }
		singleOf(::TracingPipelineBehavior)
		single { KediatRKoin.getMediator() }
	}

internal fun Application.configureKoin() {
	loadKoinModules(mediatorModule)
}
