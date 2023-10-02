package com.szastarek.text.rpg.account.adapter.rest

import com.szastarek.text.rpg.account.adapter.rest.request.*
import com.szastarek.text.rpg.account.adapter.rest.response.LogInAccountResponse
import com.szastarek.text.rpg.account.command.*
import com.szastarek.text.rpg.security.NotAuthenticatedException
import com.szastarek.text.rpg.security.authenticated
import com.szastarek.text.rpg.security.getAuthenticatedAccountContext
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
import io.ktor.server.routing.*
import org.koin.ktor.ext.inject

fun Application.configureAccountRouting() {

  val mediator by inject<Mediator>()

  install(HttpCallsExceptionHandler) {
    exception<NotAuthenticatedException> { call, _ ->
      call.respond(HttpStatusCode.Unauthorized)
    }
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

    post("${AccountApi.v1}/activate") {
      val request = call.receive<ActivateAccountRequest>()
      val command = ActivateAccountCommand(request.token).getOrThrow()
      val result = mediator.send(command)

      result.fold(
        { call.respond(HttpStatusCode.Unauthorized) },
        { call.respond(HttpStatusCode.OK) }
      )
    }

    post("${AccountApi.v1}/password/forgot") {
      val request = call.receive<ForgotPasswordRequest>()
      val command = SendResetPasswordCommand(request.email).getOrThrow()
      val result = mediator.send(command)

      result.fold(
        { call.respond(HttpStatusCode.BadRequest) },
        { call.respond(HttpStatusCode.OK) }
      )
    }

    post("${AccountApi.v1}/password/reset") {
      val request = call.receive<ResetPasswordRequest>()
      val command = ResetPasswordCommand(request.token, request.newPassword.value).getOrThrow()
      val result = mediator.send(command)

      result.fold(
        { call.respond(HttpStatusCode.Unauthorized) },
        { call.respond(HttpStatusCode.OK) }
      )
    }

    post("${AccountApi.v1}/world-creator") {
      val request = call.receive<CreateWorldCreatorAccountRequest>()
      val command = CreateWorldCreatorAccountCommand(
        request.email,
        request.password,
        request.timeZoneId,
        request.token
      ).getOrThrow()
      val result = mediator.send(command)

      result.fold(
        { call.respond(HttpStatusCode.BadRequest) },
        { call.respond(HttpStatusCode.OK) }
      )
    }

    authenticated {
      patch("${AccountApi.v1}/password") {
        val accountContext = call.getAuthenticatedAccountContext()
        val request = call.receive<ChangePasswordRequest>()
        val command = ChangePasswordCommand(
          request.currentPassword.value,
          request.newPassword.value,
          accountContext
        ).getOrThrow()
        val result = mediator.send(command)

        result.fold(
          { call.respond(HttpStatusCode.Unauthorized) },
          { call.respond(HttpStatusCode.OK) }
        )
      }

      post("${AccountApi.v1}/world-creator/invite") {
        val request = call.receive<InviteWorldCreatorRequest>()
        val command = InviteWorldCreatorCommand(request.email).getOrThrow()
        mediator.send(command)

        call.respond(HttpStatusCode.OK)
      }
    }
  }
}
