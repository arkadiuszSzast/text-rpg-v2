package com.szastarek.text.rpg.world.plugin

import com.szastarek.text.rpg.event.store.EventStoreContainer
import com.szastarek.text.rpg.event.store.EventStoreContainerFactory
import com.szastarek.text.rpg.event.store.EventStoreLifecycleListener
import com.szastarek.text.rpg.world.main
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.extensions.system.OverrideMode
import io.kotest.extensions.system.withEnvironment
import io.ktor.server.testing.TestApplication
import org.koin.test.KoinTest
import org.koin.test.check.checkModules

class KoinModulesTest : KoinTest, DescribeSpec() {
	private val eventStoreContainer: EventStoreContainer = EventStoreContainerFactory.spawn()

	init {

		listener(EventStoreLifecycleListener(eventStoreContainer))

		describe("Account Koin module test") {

			it("verify world module") {
				withEnvironment(
					mapOf(
						"DOCUMENTATION_ENABLED" to "false",
						"EVENT_STORE_CONNECTION_STRING" to eventStoreContainer.connectionString,
					),
					OverrideMode.SetOrOverride,
				) {
					TestApplication {
						application {
							main()
						}
					}.also { it.start() }
				}
				getKoin().checkModules()
			}
		}
	}
}
