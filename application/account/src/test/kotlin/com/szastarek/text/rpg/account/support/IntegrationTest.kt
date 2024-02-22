package com.szastarek.text.rpg.account.support

import com.szastarek.text.rpg.account.accountModule
import com.szastarek.text.rpg.account.adapter.mail.ActivationAccountMailVariables
import com.szastarek.text.rpg.account.adapter.mail.InviteWorldCreatorMailVariables
import com.szastarek.text.rpg.account.adapter.mail.ResetPasswordMailVariables
import com.szastarek.text.rpg.account.config.ActivateAccountMailProperties
import com.szastarek.text.rpg.account.config.InviteWorldCreatorMailProperties
import com.szastarek.text.rpg.account.config.ResetPasswordMailProperties
import com.szastarek.text.rpg.event.store.EventStoreContainer
import com.szastarek.text.rpg.event.store.EventStoreContainerFactory
import com.szastarek.text.rpg.event.store.EventStoreLifecycleListener
import com.szastarek.text.rpg.mail.MailSender
import com.szastarek.text.rpg.mail.RecordingMailSender
import com.szastarek.text.rpg.redis.RedisContainer
import com.szastarek.text.rpg.shared.email.EmailAddress
import io.kotest.core.extensions.Extension
import io.kotest.core.names.TestName
import io.kotest.core.spec.style.StringSpec
import io.kotest.core.spec.style.scopes.StringSpecScope
import io.kotest.core.spec.style.scopes.addTest
import io.kotest.extensions.system.OverrideMode
import io.kotest.extensions.system.withEnvironment
import io.ktor.client.HttpClient
import io.ktor.http.Url
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.testing.TestApplication
import org.awaitility.kotlin.await
import org.awaitility.kotlin.untilNotNull
import org.koin.core.context.loadKoinModules
import org.koin.core.context.stopKoin
import org.koin.dsl.bind
import org.koin.dsl.module
import org.koin.test.KoinTest
import org.koin.test.get
import org.redisson.api.RedissonClient
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation.Plugin as ClientContentNegotiation

abstract class IntegrationTest : StringSpec(), KoinTest {
	private val eventStoreContainer: EventStoreContainer = EventStoreContainerFactory.spawn()

	private val recordingMailSender = RecordingMailSender()
	private val redisClient: RedissonClient by lazy { get() }
	private val activateAccountMailProperties: ActivateAccountMailProperties by lazy { get() }
	private val resetPasswordMailProperties: ResetPasswordMailProperties by lazy { get() }
	private val inviteWorldCreatorMailProperties: InviteWorldCreatorMailProperties by lazy { get() }

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
					"REDIS_CONNECTION_STRING" to RedisContainer.connectionString,
				),
				OverrideMode.SetOrOverride,
			) {
				TestApplication {
					application {
						accountModule()
						loadKoinModules(
							module {
								single { recordingMailSender } bind MailSender::class
							},
						)
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
		recordingMailSender.clear()
		val redisKeys = redisClient.keys.keys
		redisClient.keys.delete(*redisKeys.toList().toTypedArray())
		test(client)
		stopKoin()
		testApplication.stop()
	}

	override fun extensions(): List<Extension> {
		return super.extensions() + EventStoreLifecycleListener(eventStoreContainer)
	}

// 	override suspend fun beforeEach(testCase: TestCase) {
// 		recordingMailSender.clear()
// 		val redisKeys = redisClient.keys.keys
// 		redisClient.keys.delete(*redisKeys.toList().toTypedArray())
// 		super.beforeTest(testCase)
// 	}

	fun getActivationToken(emailAddress: EmailAddress): String {
		return await.untilNotNull {
			recordingMailSender.getAll()
				.lastOrNull { it.to == emailAddress && it.subject == activateAccountMailProperties.subject }
				?.variables?.values
				?.get(ActivationAccountMailVariables.ACTIVATE_ACCOUNT_URL)
				?.let { Url(it).parameters["token"] }
		}
	}

	fun getResetPasswordToken(emailAddress: EmailAddress): String {
		return await.untilNotNull {
			recordingMailSender.getAll()
				.lastOrNull { it.to == emailAddress && it.subject == resetPasswordMailProperties.subject }
				?.variables?.values
				?.get(ResetPasswordMailVariables.RESET_PASSWORD_URL)
				?.let { Url(it).parameters["token"] }
		}
	}

	fun getRegisterWorldCreatorToken(emailAddress: EmailAddress): String {
		return await.untilNotNull {
			recordingMailSender.getAll()
				.lastOrNull { it.to == emailAddress && it.subject == inviteWorldCreatorMailProperties.subject }
				?.variables?.values
				?.get(InviteWorldCreatorMailVariables.REGISTER_URL)
				?.let { Url(it).parameters["token"] }
		}
	}
}
