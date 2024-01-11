package com.szastarek.text.rpg.account.adapter.rest

import com.szastarek.text.rpg.account.adapter.rest.request.ActivateAccountRequest
import com.szastarek.text.rpg.account.adapter.rest.request.ChangePasswordRequest
import com.szastarek.text.rpg.account.adapter.rest.request.CreateAccountRequest
import com.szastarek.text.rpg.account.adapter.rest.request.CreateWorldCreatorAccountRequest
import com.szastarek.text.rpg.account.adapter.rest.request.ForgotPasswordRequest
import com.szastarek.text.rpg.account.adapter.rest.request.InviteWorldCreatorRequest
import com.szastarek.text.rpg.account.adapter.rest.request.LogInAccountRequest
import com.szastarek.text.rpg.account.adapter.rest.request.RefreshTokenRequest
import com.szastarek.text.rpg.account.adapter.rest.request.ResendActivationMailRequest
import com.szastarek.text.rpg.account.adapter.rest.request.ResetPasswordRequest
import com.szastarek.text.rpg.account.adapter.rest.response.AccountDetailsResponse
import com.szastarek.text.rpg.account.adapter.rest.response.LogInAccountResponse
import com.szastarek.text.rpg.account.adapter.rest.response.RefreshTokenResponse
import com.szastarek.text.rpg.account.command.ActivateAccountCommand
import com.szastarek.text.rpg.account.command.ChangePasswordCommand
import com.szastarek.text.rpg.account.command.CreateRegularAccountCommand
import com.szastarek.text.rpg.account.command.CreateWorldCreatorAccountCommand
import com.szastarek.text.rpg.account.command.InviteWorldCreatorCommand
import com.szastarek.text.rpg.account.command.LogInAccountCommand
import com.szastarek.text.rpg.account.command.RefreshAuthTokenCommand
import com.szastarek.text.rpg.account.command.ResendActivationMailCommand
import com.szastarek.text.rpg.account.command.ResetPasswordCommand
import com.szastarek.text.rpg.account.command.SendResetPasswordCommand
import com.szastarek.text.rpg.acl.AnonymousAccountContext
import com.szastarek.text.rpg.acl.AuthenticatedAccountContext
import com.szastarek.text.rpg.acl.authority.AuthorityCheckException
import com.szastarek.text.rpg.security.NotAuthenticatedException
import com.szastarek.text.rpg.security.authenticated
import com.szastarek.text.rpg.security.getAccountContext
import com.szastarek.text.rpg.security.getAuthenticatedAccountContext
import com.szastarek.text.rpg.shared.ValidationErrorHttpMessage
import com.szastarek.text.rpg.shared.plugin.HttpCallsExceptionHandler
import com.szastarek.text.rpg.shared.validate.ValidationException
import com.szastarek.text.rpg.shared.validate.getOrThrow
import com.trendyol.kediatr.Mediator
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.application.call
import io.ktor.server.application.install
import io.ktor.server.plugins.BadRequestException
import io.ktor.server.request.receive
import io.ktor.server.request.uri
import io.ktor.server.response.respond
import io.ktor.server.routing.get
import io.ktor.server.routing.patch
import io.ktor.server.routing.post
import io.ktor.server.routing.routing
import org.koin.ktor.ext.inject

fun Application.configureAccountRouting() {
	val mediator by inject<Mediator>()

	install(HttpCallsExceptionHandler) {
		exception<NotAuthenticatedException> { call, _ ->
			call.respond(HttpStatusCode.Unauthorized)
		}
		exception<AuthorityCheckException> { call, _ ->
			call.respond(HttpStatusCode.Unauthorized)
		}
		exception<ValidationException> { call, ex ->
			call.respond(
				HttpStatusCode.BadRequest,
				ValidationErrorHttpMessage(
					ex.validationErrors,
					ex::class.java.simpleName,
					call.request.uri,
				),
			)
		}
		exception<BadRequestException> { call, ex ->
			call.respond(
				HttpStatusCode.BadRequest,
				ValidationErrorHttpMessage(
					emptyList(),
					ex::class.java.simpleName,
					call.request.uri,
				),
			)
		}
	}

	routing {
		post(AccountApi.V1) {
			val request = call.receive<CreateAccountRequest>()
			val command = CreateRegularAccountCommand(request.email, request.password.value, request.timeZoneId).getOrThrow()
			mediator.send(command)
			call.respond(HttpStatusCode.OK)
		}

		post("${AccountApi.V1}/login") {
			val request = call.receive<LogInAccountRequest>()
			val command = LogInAccountCommand(request.email, request.password).getOrThrow()
			val result = mediator.send(command)

			result.fold(
				{ call.respond(HttpStatusCode.Unauthorized) },
				{ call.respond(HttpStatusCode.OK, LogInAccountResponse(it.authToken.value, it.refreshToken.value)) },
			)
		}

		post("${AccountApi.V1}/refresh-token") {
			val request = call.receive<RefreshTokenRequest>()
			val command = RefreshAuthTokenCommand(request.accountEmail, request.refreshToken).getOrThrow()
			val result = mediator.send(command)

			result.fold(
				{ call.respond(HttpStatusCode.Unauthorized) },
				{ call.respond(HttpStatusCode.OK, RefreshTokenResponse(it.authToken.value, it.refreshToken.value)) },
			)
		}

		post("${AccountApi.V1}/activate") {
			val request = call.receive<ActivateAccountRequest>()
			val command = ActivateAccountCommand(request.token).getOrThrow()
			val result = mediator.send(command)

			result.fold(
				{ call.respond(HttpStatusCode.Unauthorized) },
				{ call.respond(HttpStatusCode.OK) },
			)
		}

		post("${AccountApi.V1}/password/forgot") {
			val request = call.receive<ForgotPasswordRequest>()
			val command = SendResetPasswordCommand(request.email).getOrThrow()
			val result = mediator.send(command)

			result.fold(
				{ call.respond(HttpStatusCode.BadRequest) },
				{ call.respond(HttpStatusCode.OK) },
			)
		}

		post("${AccountApi.V1}/password/reset") {
			val request = call.receive<ResetPasswordRequest>()
			val command = ResetPasswordCommand(request.token, request.newPassword.value).getOrThrow()
			val result = mediator.send(command)

			result.fold(
				{ call.respond(HttpStatusCode.Unauthorized) },
				{ call.respond(HttpStatusCode.OK) },
			)
		}

		post("${AccountApi.V1}/world-creator") {
			val request = call.receive<CreateWorldCreatorAccountRequest>()
			val command =
				CreateWorldCreatorAccountCommand(
					request.email,
					request.password,
					request.timeZoneId,
					request.token,
				).getOrThrow()
			val result = mediator.send(command)

			result.fold(
				{ call.respond(HttpStatusCode.BadRequest) },
				{ call.respond(HttpStatusCode.OK) },
			)
		}

		authenticated {
			get("${AccountApi.V1}/me") {
				when (val accountContext = call.getAccountContext()) {
					is AnonymousAccountContext -> call.respond(HttpStatusCode.Unauthorized)
					is AuthenticatedAccountContext ->
						call.respond(
							AccountDetailsResponse(
								accountContext.accountId.value,
								accountContext.email.value,
								accountContext.role,
							),
						)
				}
			}

			patch("${AccountApi.V1}/password") {
				val accountContext = call.getAuthenticatedAccountContext()
				val request = call.receive<ChangePasswordRequest>()
				val command =
					ChangePasswordCommand(
						request.currentPassword.value,
						request.newPassword.value,
						accountContext,
					).getOrThrow()
				val result = mediator.send(command)

				result.fold(
					{ call.respond(HttpStatusCode.Unauthorized) },
					{ call.respond(HttpStatusCode.OK) },
				)
			}

			post("${AccountApi.V1}/world-creator/invite") {
				val request = call.receive<InviteWorldCreatorRequest>()
				val command = InviteWorldCreatorCommand(request.email).getOrThrow()
				mediator.send(command)

				call.respond(HttpStatusCode.OK)
			}

			post("${AccountApi.V1}/resend-activation-mail") {
				val request = call.receive<ResendActivationMailRequest>()
				val command = ResendActivationMailCommand(request.email).getOrThrow()
				mediator.send(command)

				call.respond(HttpStatusCode.OK)
			}
		}
	}
}
