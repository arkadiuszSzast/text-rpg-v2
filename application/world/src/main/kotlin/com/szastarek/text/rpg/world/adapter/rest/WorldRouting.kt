package com.szastarek.text.rpg.world.adapter.rest

import com.szastarek.text.rpg.acl.AccountId
import com.szastarek.text.rpg.acl.AccountIdProvider
import com.szastarek.text.rpg.acl.authority.AuthorityCheckException
import com.szastarek.text.rpg.security.NotAuthenticatedException
import com.szastarek.text.rpg.shared.ValidationErrorHttpMessage
import com.szastarek.text.rpg.shared.plugin.HttpCallsExceptionHandler
import com.szastarek.text.rpg.shared.validate.ValidationException
import com.szastarek.text.rpg.shared.validate.getOrThrow
import com.szastarek.text.rpg.world.adapter.rest.request.CreateWorldDraftRequest
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
import io.ktor.server.routing.post
import io.ktor.server.routing.routing
import org.koin.ktor.ext.inject

fun Application.configureWorldRouting() {
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

// 		authenticated {
		post("${WorldApi.V1}/draft") {
			val accountIdProvider =
				object : AccountIdProvider {
					override val accountId: AccountId
						get() = AccountId("444")
				}
// 				val accountContext = call.getAuthenticatedAccountContext()
			val request = call.receive<CreateWorldDraftRequest>()
			val command = WorldDraftCreationRequestCommand(request.name, request.description, accountIdProvider).getOrThrow()

			val result = mediator.send(command)

			result.fold(
				{ call.respond(HttpStatusCode.BadRequest) },
				{ call.respond(HttpStatusCode.Accepted) }, //TODO add location header
			)
		}
// 		}
	}
}
