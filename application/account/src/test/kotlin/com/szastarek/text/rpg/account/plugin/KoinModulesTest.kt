package com.szastarek.text.rpg.account.plugin

import com.szastarek.text.rpg.account.accountModule
import com.szastarek.text.rpg.event.store.EventStoreContainer
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.extensions.system.OverrideMode
import io.kotest.extensions.system.withEnvironment
import io.kotest.koin.KoinExtension
import io.ktor.server.testing.*
import org.koin.test.KoinTest
import org.koin.test.check.checkModules

class KoinModulesTest : KoinTest, DescribeSpec() {

    init {

        describe("Account Koin module test") {

            it("verify account module") {
                withEnvironment(
                    mapOf(
                        "DOCUMENTATION_ENABLED" to "false",
                        "EVENT_STORE_CONNECTION_STRING" to EventStoreContainer.connectionString
                    ), OverrideMode.SetOrOverride
                ) {
                    TestApplication {
                        application {
                            accountModule()
                        }
                    }.also { it.start() }
                }
                getKoin().checkModules()
            }
        }
    }
}
