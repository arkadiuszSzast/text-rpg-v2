package com.szastarek.text.rpg.security.plugin

import com.szastarek.text.rpg.acl.AccountId
import com.szastarek.text.rpg.acl.AnonymousAccountContext
import com.szastarek.text.rpg.acl.AuthenticatedAccountContext
import com.szastarek.text.rpg.acl.CoroutineAccountContext
import com.szastarek.text.rpg.acl.CoroutineInjectedAuthorityContext
import com.szastarek.text.rpg.acl.RegularRole
import com.szastarek.text.rpg.acl.Role
import com.szastarek.text.rpg.acl.SuperUserRole
import com.szastarek.text.rpg.acl.authority.Authority
import com.szastarek.text.rpg.acl.authority.mergeWith
import com.szastarek.text.rpg.security.accountId
import com.szastarek.text.rpg.security.customAuthorities
import com.szastarek.text.rpg.security.role
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
        private var config = Configuration()

        override fun install(
            pipeline: ApplicationCallPipeline,
            configure: Configuration.() -> Unit
        ): AuthenticatedAccountContextPlugin {
            config = Configuration().apply(configure)
            val feature = AuthenticatedAccountContextPlugin()

            val phase = PipelinePhase("AuthenticatedAccountContextProvider")
            pipeline.insertPhaseAfter(ApplicationCallPipeline.Plugins, phase)

            pipeline.intercept(phase) {
                when (val principal = call.principal<Principal>()) {
                    is JWTPrincipal -> {
                        val accountContext = object : AuthenticatedAccountContext {
                            override val accountId: AccountId = principal.accountId
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

private fun Role.getAuthorities(): List<Authority> {
    return when(this) {
        is RegularRole -> this.authorities
        is SuperUserRole -> emptyList()
    }
}
