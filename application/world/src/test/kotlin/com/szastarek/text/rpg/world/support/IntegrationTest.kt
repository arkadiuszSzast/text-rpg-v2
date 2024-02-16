package com.szastarek.text.rpg.world.support

import com.szastarek.text.rpg.acl.AuthenticatedAccountContext
import com.szastarek.text.rpg.event.store.EventStoreContainer
import com.szastarek.text.rpg.event.store.EventStoreContainerFactory
import com.szastarek.text.rpg.event.store.EventStoreLifecycleListener
import com.szastarek.text.rpg.event.store.EventStoreProjectionsClient
import com.szastarek.text.rpg.security.AuthTokenProvider
import com.szastarek.text.rpg.world.draft.projection.WorldDraftListingByAccountIdProjectionCreator
import com.szastarek.text.rpg.world.worldModule
import io.kotest.core.extensions.Extension
import io.kotest.core.spec.Spec
import io.kotest.core.spec.style.StringSpec
import io.kotest.core.test.TestCase
import io.kotest.extensions.system.OverrideMode
import io.kotest.extensions.system.withEnvironment
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.testing.TestApplication
import org.koin.ktor.ext.getKoin
import org.koin.test.KoinTest
import org.koin.test.get
import org.koin.test.inject
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation.Plugin as ClientContentNegotiation

abstract class IntegrationTest : StringSpec(), KoinTest {
	private val eventStoreContainer: EventStoreContainer = EventStoreContainerFactory.spawn()
	private val authTokenProvider: AuthTokenProvider by lazy { AuthTokenProvider(get(), get()) }
	private val projectionsClient: EventStoreProjectionsClient by inject()

	private val testApplication =
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
			}
		}

	val client =
		testApplication.createClient {
			expectSuccess = false
			install(ClientContentNegotiation) {
				json(get())
			}
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

	override suspend fun beforeEach(testCase: TestCase) {
		WorldDraftListingByAccountIdProjectionCreator(projectionsClient).createOrUpdateAndEnable()
		super.beforeTest(testCase)
	}

	override fun afterSpec(f: suspend (Spec) -> Unit) {
		testApplication.stop()
		super.afterSpec(f)
	}
}
