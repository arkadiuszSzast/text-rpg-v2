package com.szastarek.text.rpg.security.plugin

import com.szastarek.text.rpg.acl.*
import com.szastarek.text.rpg.acl.authority.Authority
import com.szastarek.text.rpg.acl.authority.mergeWith
import com.szastarek.text.rpg.security.accountId
import com.szastarek.text.rpg.security.customAuthorities
import com.szastarek.text.rpg.security.emailAddress
import com.szastarek.text.rpg.security.role
import com.szastarek.text.rpg.shared.email.EmailAddress
import io.ktor.server.application.ApplicationCallPipeline
import io.ktor.server.application.BaseRouteScopedPlugin
import io.ktor.server.application.call
import io.ktor.server.auth.Principal
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.principal
import io.ktor.util.AttributeKey
import io.ktor.util.pipeline.PipelinePhase
import kotlinx.coroutines.withContext

class AuthenticatedAccountContextPlugin {

    class Configuration

    companion object Feature : BaseRouteScopedPlugin<Configuration, AuthenticatedAccountContextPlugin> {
        override val key = AttributeKey<AuthenticatedAccountContextPlugin>("AuthenticatedAccountContextPlugin")

        override fun install(
            pipeline: ApplicationCallPipeline,
            configure: Configuration.() -> Unit
        ): AuthenticatedAccountContextPlugin {
            val feature = AuthenticatedAccountContextPlugin()
            val phase = PipelinePhase("AuthenticatedAccountContextProvider")
            pipeline.insertPhaseAfter(ApplicationCallPipeline.Plugins, phase)

            pipeline.intercept(phase) {
                when (val principal = call.principal<Principal>()) {
                    is JWTPrincipal -> {
                        val accountContext = object : AuthenticatedAccountContext {
                            override val accountId: AccountId = principal.accountId
                            override val email: EmailAddress = principal.emailAddress
                            override suspend fun getAuthorities(): List<Authority> {
                                val roleAuthorities = role.getAuthorities()
                                val injectedAuthorities = coroutineContext[CoroutineInjectedAuthorityContext]?.authorities ?: emptyList()
                                return roleAuthorities.mergeWith(principal.customAuthorities).mergeWith(injectedAuthorities)
                            }
                            override val role: Role = principal.role
                        }

                        withContext(coroutineContext + CoroutineAccountContext(accountContext)) {
                            proceed()
                        }
                    }
                    null -> {
                        withContext(coroutineContext + CoroutineAccountContext(AnonymousAccountContext)) {
                            proceed()
                        }
                    }

                    else -> proceed()
                }
            }

            return feature
        }
    }
}
