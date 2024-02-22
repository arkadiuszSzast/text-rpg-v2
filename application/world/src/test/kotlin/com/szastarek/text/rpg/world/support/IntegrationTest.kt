package com.szastarek.text.rpg.world.support

import com.szastarek.text.rpg.acl.AuthenticatedAccountContext
import com.szastarek.text.rpg.event.store.EventStoreContainer
import com.szastarek.text.rpg.event.store.EventStoreContainerFactory
import com.szastarek.text.rpg.event.store.EventStoreLifecycleListener
import com.szastarek.text.rpg.security.AuthTokenProvider
import com.szastarek.text.rpg.world.worldModule
import io.kotest.core.extensions.Extension
import io.kotest.core.names.TestName
import io.kotest.core.spec.style.StringSpec
import io.kotest.core.spec.style.scopes.StringSpecScope
import io.kotest.core.spec.style.scopes.addTest
import io.kotest.extensions.system.OverrideMode
import io.kotest.extensions.system.withEnvironment
import io.ktor.client.HttpClient
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.testing.TestApplication
import org.koin.core.context.stopKoin
import org.koin.test.KoinTest
import org.koin.test.get
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation.Plugin as ClientContentNegotiation

abstract class IntegrationTest : StringSpec(), KoinTest {
	private val eventStoreContainer: EventStoreContainer = EventStoreContainerFactory.spawn()
	private val authTokenProvider: AuthTokenProvider by lazy { AuthTokenProvider(get(), get()) }

	operator fun String.invoke(test: suspend StringSpecScope.(client: HttpClient) -> Unit) {
		addTest(TestName(null, this, false), false, null) {
			StringSpecScope(this.coroutineContext, testCase).withClient(test)
		}
	}

	private suspend fun StringSpecScope.withClient(test: suspend StringSpecScope.(client: HttpClient) -> Unit) {
		val testApplication =
			withEnvironment(
				mapOf(
					"DOCUMENTATION_ENABLED" to "false",
					"EVENT_STORE_CONNECTION_STRING" to eventStoreContainer.connectionString,
				),
				OverrideMode.SetOrOverride,
			) {
				TestApplication {
					application {
						worldModule()
						getKoin().createEagerInstances()
					}
				}.also {
					it.start()
					println("APPLICATION STARTED")
				}
			}

		val client =
			testApplication.createClient {
				expectSuccess = false
				install(ClientContentNegotiation) {
					json(get())
				}
			}
		test(client)
		stopKoin()
		testApplication.stop()
	}

	suspend fun getAuthToken(accountContext: AuthenticatedAccountContext) =
		authTokenProvider.createAuthToken(
			accountContext.accountId,
			accountContext.email,
			accountContext.role,
			accountContext.getAuthorities(),
		)

	override fun extensions(): List<Extension> {
		return super.extensions() + EventStoreLifecycleListener(eventStoreContainer)
	}
}
