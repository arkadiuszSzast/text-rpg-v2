package com.szastarek.text.rpg.world.support

import com.szastarek.text.rpg.security.JwtToken
import com.szastarek.text.rpg.world.adapter.rest.WorldApi
import com.szastarek.text.rpg.world.adapter.rest.request.InitializeWorldDraftCreationRequest
import io.ktor.client.HttpClient
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.http.ContentType
import io.ktor.http.contentType

internal suspend fun HttpClient.initializeWorldDraftCreation(
	request: InitializeWorldDraftCreationRequest,
	authToken: JwtToken,
): HttpResponse {
	return post("${WorldApi.V1}/draft") {
		contentType(ContentType.Application.Json)
		bearerAuth(authToken.value)
		setBody(request)
	}
}
