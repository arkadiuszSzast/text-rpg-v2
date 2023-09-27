package com.szastarek.text.rpg.mail.adapter.sendgrid

import com.szastarek.text.rpg.mail.config.SendGridProperties
import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktor.client.*
import io.ktor.client.engine.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.logging.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.http.URLProtocol.Companion.HTTPS
import io.ktor.serialization.kotlinx.json.*
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation.Plugin as ClientContentNegotiation

class SendgridClient(
  engine: HttpClientEngine,
  private val sendGridProperties: SendGridProperties
) {
  private val logger = KotlinLogging.logger {}

  private val httpClient = HttpClient(engine) {
    install(ClientContentNegotiation) {
      json()
    }
    install(Logging) {
      logger = object : Logger {
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