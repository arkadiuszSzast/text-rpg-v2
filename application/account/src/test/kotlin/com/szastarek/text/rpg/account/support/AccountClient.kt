package com.szastarek.text.rpg.account.support

import com.szastarek.text.rpg.account.adapter.rest.AccountApi
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
import com.szastarek.text.rpg.security.JwtToken
import io.ktor.client.HttpClient
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.get
import io.ktor.client.request.patch
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.http.ContentType
import io.ktor.http.contentType

internal suspend fun HttpClient.createAccount(request: CreateAccountRequest): HttpResponse {
	return post(AccountApi.V1) {
		contentType(ContentType.Application.Json)
		setBody(request)
	}
}

internal suspend fun HttpClient.activateAccount(request: ActivateAccountRequest): HttpResponse {
	return post("${AccountApi.V1}/activate") {
		contentType(ContentType.Application.Json)
		setBody(request)
	}
}

internal suspend fun HttpClient.logIn(request: LogInAccountRequest): HttpResponse {
	return post("${AccountApi.V1}/login") {
		contentType(ContentType.Application.Json)
		setBody(request)
	}
}

internal suspend fun HttpClient.refreshToken(request: RefreshTokenRequest): HttpResponse {
	return post("${AccountApi.V1}/refresh-token") {
		contentType(ContentType.Application.Json)
		setBody(request)
	}
}

internal suspend fun HttpClient.me(authToken: JwtToken): HttpResponse {
	return get("${AccountApi.V1}/me") {
		contentType(ContentType.Application.Json)
		bearerAuth(authToken.value)
	}
}

internal suspend fun HttpClient.forgotPassword(request: ForgotPasswordRequest): HttpResponse {
	return post("${AccountApi.V1}/password/forgot") {
		contentType(ContentType.Application.Json)
		setBody(request)
	}
}

internal suspend fun HttpClient.resetPassword(request: ResetPasswordRequest): HttpResponse {
	return post("${AccountApi.V1}/password/reset") {
		contentType(ContentType.Application.Json)
		setBody(request)
	}
}

internal suspend fun HttpClient.changePassword(
	request: ChangePasswordRequest,
	authToken: JwtToken,
): HttpResponse {
	return patch("${AccountApi.V1}/password") {
		contentType(ContentType.Application.Json)
		bearerAuth(authToken.value)
		setBody(request)
	}
}

internal suspend fun HttpClient.inviteWorldCreator(
	request: InviteWorldCreatorRequest,
	authToken: JwtToken,
): HttpResponse {
	return post("${AccountApi.V1}/world-creator/invite") {
		contentType(ContentType.Application.Json)
		bearerAuth(authToken.value)
		setBody(request)
	}
}

internal suspend fun HttpClient.createWorldCreatorAccount(request: CreateWorldCreatorAccountRequest): HttpResponse {
	return post("${AccountApi.V1}/world-creator") {
		contentType(ContentType.Application.Json)
		setBody(request)
	}
}

internal suspend fun HttpClient.resendActivationMail(
	request: ResendActivationMailRequest,
	authToken: JwtToken,
): HttpResponse {
	return post("${AccountApi.V1}/resend-activation-mail") {
		contentType(ContentType.Application.Json)
		bearerAuth(authToken.value)
		setBody(request)
	}
}
