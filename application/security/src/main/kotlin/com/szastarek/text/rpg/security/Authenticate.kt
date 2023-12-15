package com.szastarek.text.rpg.security

import com.szastarek.text.rpg.security.plugin.AuthenticatedAccountContextPlugin
import io.ktor.server.application.install
import io.ktor.server.auth.authenticate
import io.ktor.server.routing.Route

fun Route.authenticated(
	vararg configurations: String? = arrayOf(null),
	optional: Boolean = false,
	build: Route.() -> Unit,
): Route {
	return authenticate(*configurations, optional = optional) {
		install(AuthenticatedAccountContextPlugin)
		build()
	}
}
