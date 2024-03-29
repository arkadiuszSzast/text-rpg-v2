package com.szastarek.text.rpg.world.adapter.rest

import com.szastarek.text.rpg.acl.AccountId
import com.szastarek.text.rpg.acl.Roles
import com.szastarek.text.rpg.acl.authority.AuthorityCheckException
import com.szastarek.text.rpg.security.AuthTokenProvider
import com.szastarek.text.rpg.security.NotAuthenticatedException
import com.szastarek.text.rpg.security.authenticated
import com.szastarek.text.rpg.security.getAuthenticatedAccountContext
import com.szastarek.text.rpg.shared.ProblemHttpErrorResponse
import com.szastarek.text.rpg.shared.ValidationErrorHttpMessage
import com.szastarek.text.rpg.shared.email.EmailAddress
import com.szastarek.text.rpg.shared.plugin.HttpCallsExceptionHandler
import com.szastarek.text.rpg.shared.validate.ValidationException
import com.szastarek.text.rpg.shared.validate.getOrThrow
import com.szastarek.text.rpg.world.adapter.rest.request.InitializeWorldDraftCreationRequest
import com.szastarek.text.rpg.world.draft.command.WorldDraftCreationRequestCommand
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
import io.ktor.server.routing.post
import io.ktor.server.routing.routing
import org.koin.ktor.ext.inject

fun Application.configureWorldRouting() {
	val mediator by inject<Mediator>()
	val authTokenProvider: AuthTokenProvider by inject()

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
		get("/world-creator-auth-token") {
			call.respond(
				authTokenProvider.createAuthToken(
					AccountId("world-creator"),
					EmailAddress("arek@test.com").getOrThrow(),
					Roles.WorldCreator.role,
					emptyList(),
				).value,
			)
		}

		authenticated {
			post("${WorldApi.V1}/draft") {
				val request = call.receive<InitializeWorldDraftCreationRequest>()
				val accountContext = call.getAuthenticatedAccountContext()
				val command = WorldDraftCreationRequestCommand(request.name, accountContext).getOrThrow()

				val result = mediator.send(command)

				result.fold(
					{
						call.respond(
							HttpStatusCode.BadRequest,
							ProblemHttpErrorResponse(
								"create_draft_error",
								"Failed to create world draft",
								call.request.uri,
								errors = it.map { it.name },
							),
						)
					},
					{ call.respond(HttpStatusCode.Accepted) },
				)
			}
		}
	}
}
