package com.szastarek.text.rpg.account.support

import com.szastarek.text.rpg.account.adapter.rest.AccountApi
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.statement.HttpResponse

internal suspend fun HttpClient.helloWorld(): HttpResponse {
    return get(AccountApi.v1)
}