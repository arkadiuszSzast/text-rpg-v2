package com.szastarek.text.rpg.account.adapter.rest

import com.szastarek.text.rpg.account.adapter.rest.request.CreateAccountRequest
import com.szastarek.text.rpg.account.adapter.rest.request.LogInAccountRequest
import com.szastarek.text.rpg.account.adapter.rest.response.LogInAccountResponse
import com.szastarek.text.rpg.account.command.CreateRegularAccountCommand
import com.szastarek.text.rpg.account.command.LogInAccountCommand
import com.szastarek.text.rpg.shared.ValidationErrorHttpMessage
import com.szastarek.text.rpg.shared.plugin.HttpCallsExceptionHandler
import com.szastarek.text.rpg.shared.validate.ValidationException
import com.szastarek.text.rpg.shared.validate.getOrThrow
import com.trendyol.kediatr.Mediator
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.*
import io.ktor.server.application.Application
import io.ktor.server.application.call
import io.ktor.server.request.receive
import io.ktor.server.request.uri
import io.ktor.server.response.respond
import io.ktor.server.routing.post
import io.ktor.server.routing.routing
import org.koin.ktor.ext.inject

fun Application.configureAccountRouting() {

  val mediator by inject<Mediator>()

  install(HttpCallsExceptionHandler) {
    exception<ValidationException> { call, ex ->
      call.respond(
        HttpStatusCode.BadRequest, ValidationErrorHttpMessage(
          ex.validationErrors,
          ex::class.java.simpleName,
          call.request.uri
        )
      )
    }
  }

  routing {
    post(AccountApi.v1) {
      val request = call.receive<CreateAccountRequest>()
      val command = CreateRegularAccountCommand(request.email, request.password.value, request.timeZoneId).getOrThrow()
      mediator.send(command)
      call.respond(HttpStatusCode.OK)
    }

    post("${AccountApi.v1}/login") {
      val request = call.receive<LogInAccountRequest>()
      val command = LogInAccountCommand(request.email, request.password).getOrThrow()
      val result = mediator.send(command)

      result.fold(
        { call.respond(HttpStatusCode.Unauthorized) },
        { call.respond(HttpStatusCode.OK, LogInAccountResponse(it.token.value)) }
      )
    }
  }
}
