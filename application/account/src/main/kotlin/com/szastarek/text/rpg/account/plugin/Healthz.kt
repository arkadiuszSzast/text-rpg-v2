package com.szastarek.text.rpg.account.plugin

import com.szastarek.text.rpg.shared.plugin.HealthzPlugin
import io.ktor.server.application.Application
import io.ktor.server.application.install

internal fun Application.configureHealthz() {
	install(HealthzPlugin) {
		healthChecks {
			check("main") { true }
		}
		readyChecks {
			check("main") { true }
		}
	}
}
