package com.szastarek.text.rpg.world.adapter.rest

import com.szastarek.text.rpg.acl.worldCreatorAuthenticatedAccountContext
import com.szastarek.text.rpg.world.support.IntegrationTest
import com.szastarek.text.rpg.world.support.anInitializeWorldDraftCreationRequest
import com.szastarek.text.rpg.world.support.initializeWorldDraftCreation
import io.kotest.matchers.shouldBe
import io.ktor.http.HttpStatusCode

class WorldRoutingKtTest : IntegrationTest() {
	init {

		"should return 202 when world draft creation initialized" {
			val accountContext = worldCreatorAuthenticatedAccountContext
			val authToken = getAuthToken(accountContext)
			val request = anInitializeWorldDraftCreationRequest()

			client.initializeWorldDraftCreation(request, authToken)
				.status.shouldBe(HttpStatusCode.Accepted)
		}
	}
}
