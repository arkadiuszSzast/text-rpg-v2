package com.szastarek.text.rpg.mail.adapter.sendgrid

import com.szastarek.text.rpg.mail.config.SendGridProperties
import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktor.client.HttpClient
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.plugins.HttpRequestRetry
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.URLProtocol.Companion.HTTPS
import io.ktor.http.contentType
import io.ktor.http.headers
import io.ktor.http.path
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation.Plugin as ClientContentNegotiation

class SendgridClient(
	engine: HttpClientEngine,
	private val sendGridProperties: SendGridProperties,
) {
	private val logger = KotlinLogging.logger {}

	private val httpClient =
		HttpClient(engine) {
			install(ClientContentNegotiation) {
				json(Json { ignoreUnknownKeys = true })
			}
			install(Logging) {
				logger =
					object : Logger {
						override fun log(message: String) {
							this@SendgridClient.logger.debug { message }
						}
					}
				level = LogLevel.ALL
				sanitizeHeader { it == HttpHeaders.Authorization }
			}
			install(HttpRequestRetry) {
				retryOnServerErrors(5)
				exponentialDelay()
			}
		}

	internal suspend fun sendMail(request: SendgridSendMailRequest) =
		httpClient.post {
			url {
				protocol = HTTPS
				host = sendGridProperties.host
				path("v3", "mail", "send")
			}
			headers {
				bearerAuth(sendGridProperties.apiKey)
				contentType(ContentType.Application.Json)
			}
			setBody(request)
		}
}
