package com.szastarek.text.rpg.world.adapter.rest

import com.szastarek.text.rpg.acl.worldCreatorAuthenticatedAccountContext
import com.szastarek.text.rpg.shared.ProblemHttpErrorResponse
import com.szastarek.text.rpg.world.draft.command.WorldDraftCreationRequestError
import com.szastarek.text.rpg.world.support.IntegrationTest
import com.szastarek.text.rpg.world.support.anInitializeWorldDraftCreationRequest
import com.szastarek.text.rpg.world.support.initializeWorldDraftCreation
import io.kotest.matchers.shouldBe
import io.ktor.client.call.body
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.delay

class WorldRoutingKtTest : IntegrationTest() {
	init {

		"should return 202 when world draft creation initialized" { client ->
			// arrange
			val accountContext = worldCreatorAuthenticatedAccountContext
			val authToken = getAuthToken(accountContext)
			val request = anInitializeWorldDraftCreationRequest()

			// act && assert
			client.initializeWorldDraftCreation(request, authToken)
				.status.shouldBe(HttpStatusCode.Accepted)
		}

		"world creator cannot create more than 3 drafts" { client ->
			// arrange
			val accountContext = worldCreatorAuthenticatedAccountContext
			val authToken = getAuthToken(accountContext)
			client.initializeWorldDraftCreation(anInitializeWorldDraftCreationRequest(), authToken)
				.status.shouldBe(HttpStatusCode.Accepted)
			client.initializeWorldDraftCreation(anInitializeWorldDraftCreationRequest(), authToken)
				.status.shouldBe(HttpStatusCode.Accepted)
			client.initializeWorldDraftCreation(anInitializeWorldDraftCreationRequest(), authToken)
				.status.shouldBe(HttpStatusCode.Accepted)
			delay(1000)

			// act
			val response = client.initializeWorldDraftCreation(anInitializeWorldDraftCreationRequest(), authToken)

			// assert
			response.status.shouldBe(HttpStatusCode.BadRequest)
			response.body<ProblemHttpErrorResponse>().errors
				.shouldBe(listOf(WorldDraftCreationRequestError.MaximumNumberOfDraftsReached.name))
		}
	}
}
