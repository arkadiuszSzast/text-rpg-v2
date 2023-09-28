package com.szastarek.text.rpg.account.support

import com.szastarek.text.rpg.account.accountModule
import com.szastarek.text.rpg.event.store.*
import com.szastarek.text.rpg.mail.MailSender
import com.szastarek.text.rpg.mail.RecordingMailSender
import io.kotest.core.spec.Spec
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.core.test.TestCase
import io.kotest.extensions.system.OverrideMode
import io.kotest.extensions.system.withEnvironment
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.testing.TestApplication
import org.koin.core.context.loadKoinModules
import org.koin.dsl.bind
import org.koin.dsl.module
import org.koin.test.KoinTest
import org.koin.test.get
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation.Plugin as ClientContentNegotiation

abstract class IntegrationTest : DescribeSpec(), KoinTest {

  private val recordingMailSender = RecordingMailSender()

  private val testApplication = withEnvironment(
    mapOf(
      "DOCUMENTATION_ENABLED" to "false",
      "EVENT_STORE_CONNECTION_STRING" to EventStoreContainer.connectionString
    ), OverrideMode.SetOrOverride
  ) {
    TestApplication {
      application {
        accountModule()
        loadKoinModules(module {
          single { recordingMailSender } bind MailSender::class
        })
        getKoin().createEagerInstances()
      }
    }.also {
      it.start()
    }
  }

  val client = testApplication.createClient {
    expectSuccess = false
    install(ClientContentNegotiation) {
      json(get())
    }
  }

  override suspend fun beforeEach(testCase: TestCase) {
    EventStoreContainer.restart()
    recordingMailSender.clear()
    super.beforeTest(testCase)
  }

  override fun afterSpec(f: suspend (Spec) -> Unit) {
    testApplication.stop()
    super.afterSpec(f)
  }

}