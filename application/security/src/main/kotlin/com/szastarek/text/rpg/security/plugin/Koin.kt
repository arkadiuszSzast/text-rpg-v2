package com.szastarek.text.rpg.security.plugin

import com.szastarek.text.rpg.acl.AccountContextProvider
import com.szastarek.text.rpg.acl.authority.AuthorizedAccountAbilityProvider
import com.szastarek.text.rpg.acl.authority.DefaultAuthorizedAccountAbilityProvider
import com.szastarek.text.rpg.security.AuthTokenProvider
import com.szastarek.text.rpg.security.CoroutineAccountContextProvider
import com.szastarek.text.rpg.security.config.AuthenticationProperties
import com.szastarek.text.rpg.security.config.CorsProperties
import com.szastarek.text.rpg.shared.config.ConfigKey
import com.szastarek.text.rpg.shared.config.getListProperty
import com.szastarek.text.rpg.shared.config.getLongProperty
import com.szastarek.text.rpg.shared.config.getStringProperty
import io.ktor.server.application.Application
import kotlinx.datetime.Clock
import org.koin.core.context.loadKoinModules
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module
import kotlin.time.Duration.Companion.milliseconds

internal val securityModule =
	module {
		single {
			AuthenticationProperties(
				jwtAudience = getStringProperty(ConfigKey("authentication.jwt.audience")),
				jwtIssuer = getStringProperty(ConfigKey("authentication.jwt.issuer")),
				jwtRealm = getStringProperty(ConfigKey("authentication.jwt.realm")),
				jwtSecret = getStringProperty(ConfigKey("authentication.jwt.secret")),
				authTokenExpiration = getLongProperty(ConfigKey("authentication.jwt.authExpirationInMillis")).milliseconds,
			)
		}
		single {
			CorsProperties(
				allowedHosts = getListProperty(ConfigKey("cors.allowedHosts")),
			)
		}
		singleOf(::AuthTokenProvider)
		singleOf(::CoroutineAccountContextProvider) bind AccountContextProvider::class
		singleOf(::DefaultAuthorizedAccountAbilityProvider) bind AuthorizedAccountAbilityProvider::class
		single { Clock.System } bind Clock::class
	}

internal fun Application.configureKoin() {
	loadKoinModules(securityModule)
}
