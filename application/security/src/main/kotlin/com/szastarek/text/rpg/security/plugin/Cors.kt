package com.szastarek.text.rpg.security.plugin

import com.szastarek.text.rpg.security.config.CorsProperties
import com.szastarek.text.rpg.shared.plugin.installIfNotRegistered
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.server.application.Application
import io.ktor.server.plugins.cors.routing.CORS

fun Application.configureCors(corsProperties: CorsProperties) {
	installIfNotRegistered(CORS) {
		allowMethod(HttpMethod.Options)
		allowMethod(HttpMethod.Put)
		allowMethod(HttpMethod.Delete)
		allowMethod(HttpMethod.Patch)
		allowHeader(HttpHeaders.Authorization)

		corsProperties.allowedHosts.forEach { host ->
			allowHost(host)
		}
	}
}
