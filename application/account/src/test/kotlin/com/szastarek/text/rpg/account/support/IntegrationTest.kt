package com.szastarek.text.rpg.account.support

import com.szastarek.text.rpg.account.accountModule
import com.szastarek.text.rpg.account.adapter.mail.ActivationAccountMailVariables
import com.szastarek.text.rpg.account.adapter.mail.InviteWorldCreatorMailVariables
import com.szastarek.text.rpg.account.adapter.mail.ResetPasswordMailVariables
import com.szastarek.text.rpg.account.config.ActivateAccountMailProperties
import com.szastarek.text.rpg.account.config.InviteWorldCreatorMailProperties
import com.szastarek.text.rpg.account.config.ResetPasswordMailProperties
import com.szastarek.text.rpg.event.store.*
import com.szastarek.text.rpg.mail.MailSender
import com.szastarek.text.rpg.mail.RecordingMailSender
import com.szastarek.text.rpg.redis.RedisContainer
import com.szastarek.text.rpg.shared.email.EmailAddress
import io.kotest.core.extensions.Extension
import io.kotest.core.spec.Spec
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.core.test.TestCase
import io.kotest.extensions.system.OverrideMode
import io.kotest.extensions.system.withEnvironment
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.testing.TestApplication
import org.awaitility.kotlin.await
import org.awaitility.kotlin.untilNotNull
import org.koin.core.context.loadKoinModules
import org.koin.dsl.bind
import org.koin.dsl.module
import org.koin.test.KoinTest
import org.koin.test.get
import org.koin.test.inject
import org.redisson.api.RedissonClient
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation.Plugin as ClientContentNegotiation

abstract class IntegrationTest : DescribeSpec(), KoinTest {

  private val eventStoreContainer: EventStoreContainer = EventStoreContainerFactory.spawn()

  private val recordingMailSender = RecordingMailSender()
  private val redisClient by inject<RedissonClient>()
  private val activateAccountMailProperties by inject<ActivateAccountMailProperties>()
  private val resetPasswordMailProperties by inject<ResetPasswordMailProperties>()
  private val inviteWorldCreatorMailProperties by inject<InviteWorldCreatorMailProperties>()

  private val testApplication = withEnvironment(
    mapOf(
      "DOCUMENTATION_ENABLED" to "false",
      "EVENT_STORE_CONNECTION_STRING" to eventStoreContainer.connectionString,
      "REDIS_CONNECTION_STRING" to RedisContainer.connectionString
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

  override fun extensions(): List<Extension> {
    return super.extensions() + EventStoreLifecycleListener(eventStoreContainer)
  }

  override suspend fun beforeEach(testCase: TestCase) {
    recordingMailSender.clear()
    val redisKeys = redisClient.keys.keys
    redisClient.keys.delete(*redisKeys.toList().toTypedArray())
    super.beforeTest(testCase)
  }

  override fun afterSpec(f: suspend (Spec) -> Unit) {
    testApplication.stop()
    super.afterSpec(f)
  }

  fun getActivationToken(emailAddress: EmailAddress): String {
    return await.untilNotNull {
      recordingMailSender.getAll()
      .lastOrNull { it.to == emailAddress && it.subject == activateAccountMailProperties.subject }
      ?.variables?.values
      ?.get(ActivationAccountMailVariables.activateAccountUrl)
      ?.let { Url(it).parameters["token"] }
    }
  }

  fun getResetPasswordToken(emailAddress: EmailAddress): String {
    return await.untilNotNull { recordingMailSender.getAll()
      .lastOrNull { it.to == emailAddress && it.subject == resetPasswordMailProperties.subject }
      ?.variables?.values
      ?.get(ResetPasswordMailVariables.resetPasswordUrl)
      ?.let { Url(it).parameters["token"] } }
  }

  fun getRegisterWorldCreatorToken(emailAddress: EmailAddress): String {
    return await.untilNotNull { recordingMailSender.getAll()
      .lastOrNull { it.to == emailAddress && it.subject == inviteWorldCreatorMailProperties.subject }
      ?.variables?.values
      ?.get(InviteWorldCreatorMailVariables.registerUrl)
      ?.let { Url(it).parameters["token"] } }
  }

}
