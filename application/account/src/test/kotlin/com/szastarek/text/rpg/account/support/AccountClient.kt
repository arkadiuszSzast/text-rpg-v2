package com.szastarek.text.rpg.account.support

import com.szastarek.text.rpg.account.adapter.rest.AccountApi
import com.szastarek.text.rpg.account.adapter.rest.request.*
import com.szastarek.text.rpg.security.JwtToken
import io.ktor.client.HttpClient
import io.ktor.client.request.*
import io.ktor.client.statement.HttpResponse
import io.ktor.http.ContentType
import io.ktor.http.contentType

internal suspend fun HttpClient.createAccount(request: CreateAccountRequest): HttpResponse {
    return post(AccountApi.v1) {
        contentType(ContentType.Application.Json)
        setBody(request)
    }
}

internal suspend fun HttpClient.activateAccount(request: ActivateAccountRequest): HttpResponse {
    return post("${AccountApi.v1}/activate") {
        contentType(ContentType.Application.Json)
        setBody(request)
    }
}

internal suspend fun HttpClient.logIn(request: LogInAccountRequest): HttpResponse {
    return post("${AccountApi.v1}/login") {
        contentType(ContentType.Application.Json)
        setBody(request)
    }
}

internal suspend fun HttpClient.forgotPassword(request: ForgotPasswordRequest): HttpResponse {
    return post("${AccountApi.v1}/password/forgot") {
        contentType(ContentType.Application.Json)
        setBody(request)
    }
}

internal suspend fun HttpClient.resetPassword(request: ResetPasswordRequest): HttpResponse {
    return post("${AccountApi.v1}/password/reset") {
        contentType(ContentType.Application.Json)
        setBody(request)
    }
}

internal suspend fun HttpClient.changePassword(request: ChangePasswordRequest, authToken: JwtToken): HttpResponse {
    return patch("${AccountApi.v1}/password") {
        contentType(ContentType.Application.Json)
        bearerAuth(authToken.value)
        setBody(request)
    }
}

internal suspend fun HttpClient.inviteWorldCreator(request: InviteWorldCreatorRequest, authToken: JwtToken): HttpResponse {
    return post("${AccountApi.v1}/world-creator/invite") {
        contentType(ContentType.Application.Json)
        bearerAuth(authToken.value)
        setBody(request)
    }
}

internal suspend fun HttpClient.createWorldCreatorAccount(request: CreateWorldCreatorAccountRequest): HttpResponse {
    return post("${AccountApi.v1}/world-creator") {
        contentType(ContentType.Application.Json)
        setBody(request)
    }
}
