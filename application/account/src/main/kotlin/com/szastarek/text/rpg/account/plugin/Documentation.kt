package com.szastarek.text.rpg.account.plugin

import com.szastarek.text.rpg.account.config.DocumentationProperties
import io.ktor.server.application.Application
import io.ktor.server.plugins.swagger.swaggerUI
import io.ktor.server.routing.routing

internal fun Application.configureDocumentation(documentationProperties: DocumentationProperties) {
	if (documentationProperties.enabled) {
		routing {
			swaggerUI(path = "account-openapi", "openapi/account-documentation.yaml")
		}
	}
}
