package com.szastarek.text.rpg.security

import com.szastarek.text.rpg.security.plugin.configureAuthentication
import com.szastarek.text.rpg.security.plugin.configureCors
import com.szastarek.text.rpg.security.plugin.configureKoin
import io.ktor.server.application.Application
import org.koin.ktor.ext.get

fun Application.securityModule() {
	configureKoin()
	configureAuthentication(get())
	configureCors(get())
}
