package com.szastarek.text.rpg.account.support

import com.szastarek.text.rpg.account.accountModule
import io.kotest.core.spec.Spec
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.extensions.system.OverrideMode
import io.kotest.extensions.system.withEnvironment
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.testing.TestApplication
import org.koin.test.KoinTest
import org.koin.test.get
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation.Plugin as ClientContentNegotiation

open class IntegrationTest : KoinTest, DescribeSpec() {

    private val testApplication = withEnvironment(
        mapOf("DOCUMENTATION_ENABLED" to "false"), OverrideMode.SetOrOverride
    ) {
        TestApplication {
            application {
                accountModule()
            }
        }.also { it.start() }
    }

    val client = testApplication.createClient {
        expectSuccess = false
        install(ClientContentNegotiation) {
            json(get())
        }
    }

    override fun afterSpec(f: suspend (Spec) -> Unit) {
        testApplication.stop()
    }

}