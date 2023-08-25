package com.szastarek.text.rpg.security.plugin

import com.szastarek.text.rpg.security.config.AuthenticationProperties
import com.szastarek.text.rpg.security.config.CorsProperties
import com.szastarek.text.rpg.shared.config.ConfigKey
import com.szastarek.text.rpg.shared.config.getListProperty
import com.szastarek.text.rpg.shared.config.getLongProperty
import com.szastarek.text.rpg.shared.config.getStringProperty
import io.ktor.server.application.Application
import org.koin.core.context.loadKoinModules
import org.koin.dsl.module

internal val securityModule = module {
    single {
        AuthenticationProperties(
            jwtAudience = getStringProperty(ConfigKey("authentication.jwt.audience")),
            jwtIssuer = getStringProperty(ConfigKey("authentication.jwt.issuer")),
            jwtRealm = getStringProperty(ConfigKey("authentication.jwt.realm")),
            jwtSecret = getStringProperty(ConfigKey("authentication.jwt.secret")),
            expirationInMillis = getLongProperty(ConfigKey("authentication.jwt.expirationInMillis"))
        )
    }
    single {
        CorsProperties(
            allowedHosts = getListProperty(ConfigKey("cors.allowedHosts"))
        )
    }
}

internal fun Application.configureKoin() {
    loadKoinModules(securityModule)
}
