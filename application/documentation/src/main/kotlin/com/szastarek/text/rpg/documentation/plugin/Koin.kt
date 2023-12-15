package com.szastarek.text.rpg.documentation.plugin

import com.szastarek.text.rpg.documentation.config.DocumentationProperties
import com.szastarek.text.rpg.shared.config.ConfigKey
import com.szastarek.text.rpg.shared.config.getBooleanProperty
import io.ktor.server.application.Application
import org.koin.core.context.loadKoinModules
import org.koin.dsl.module

internal val documentationModule =
	module {
		single {
			DocumentationProperties(
				enabled = getBooleanProperty(ConfigKey("documentation.enabled")),
			)
		}
	}

internal fun Application.configureKoin() {
	loadKoinModules(documentationModule)
}
