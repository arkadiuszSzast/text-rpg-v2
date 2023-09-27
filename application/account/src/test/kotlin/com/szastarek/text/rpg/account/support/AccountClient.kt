package com.szastarek.text.rpg.account.support

import com.szastarek.text.rpg.account.adapter.rest.AccountApi
import com.szastarek.text.rpg.account.adapter.rest.request.CreateAccountRequest
import io.ktor.client.HttpClient
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.http.ContentType
import io.ktor.http.contentType

internal suspend fun HttpClient.createAccount(request: CreateAccountRequest): HttpResponse {
    return post(AccountApi.v1) {
        contentType(ContentType.Application.Json)
        setBody(request)
    }
}
