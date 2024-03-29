package com.szastarek.text.rpg.account.adapter.rest

import com.szastarek.text.rpg.account.AccountStatus
import com.szastarek.text.rpg.account.adapter.rest.request.ActivateAccountRequest
import com.szastarek.text.rpg.account.adapter.rest.request.ChangePasswordRequest
import com.szastarek.text.rpg.account.adapter.rest.request.CreateAccountRequest
import com.szastarek.text.rpg.account.adapter.rest.request.ForgotPasswordRequest
import com.szastarek.text.rpg.account.adapter.rest.request.InviteWorldCreatorRequest
import com.szastarek.text.rpg.account.adapter.rest.request.LogInAccountRequest
import com.szastarek.text.rpg.account.adapter.rest.request.RefreshTokenRequest
import com.szastarek.text.rpg.account.adapter.rest.request.ResendActivationMailRequest
import com.szastarek.text.rpg.account.adapter.rest.request.ResetPasswordRequest
import com.szastarek.text.rpg.account.adapter.rest.response.AccountDetailsResponse
import com.szastarek.text.rpg.account.adapter.rest.response.LogInAccountResponse
import com.szastarek.text.rpg.account.event.AccountCreatedEvent
import com.szastarek.text.rpg.account.event.AccountEvent
import com.szastarek.text.rpg.account.support.IntegrationTest
import com.szastarek.text.rpg.account.support.aCreateAccountRequest
import com.szastarek.text.rpg.account.support.aCreateWorldCreatorAccountRequest
import com.szastarek.text.rpg.account.support.activateAccount
import com.szastarek.text.rpg.account.support.anAccountCreatedEvent
import com.szastarek.text.rpg.account.support.changePassword
import com.szastarek.text.rpg.account.support.createAccount
import com.szastarek.text.rpg.account.support.createWorldCreatorAccount
import com.szastarek.text.rpg.account.support.forgotPassword
import com.szastarek.text.rpg.account.support.inviteWorldCreator
import com.szastarek.text.rpg.account.support.logIn
import com.szastarek.text.rpg.account.support.me
import com.szastarek.text.rpg.account.support.refreshToken
import com.szastarek.text.rpg.account.support.resendActivationMail
import com.szastarek.text.rpg.account.support.resetPassword
import com.szastarek.text.rpg.acl.Roles
import com.szastarek.text.rpg.event.store.EventStoreWriteClient
import com.szastarek.text.rpg.event.store.appendToStream
import com.szastarek.text.rpg.security.JwtToken
import com.szastarek.text.rpg.shared.MaskedString
import com.szastarek.text.rpg.shared.ValidationErrorHttpMessage
import com.szastarek.text.rpg.shared.aRawPassword
import com.szastarek.text.rpg.shared.anEmail
import com.szastarek.text.rpg.shared.email.EmailAddress
import com.szastarek.text.rpg.shared.validate.getOrThrow
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.should
import io.kotest.matchers.shouldBe
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.http.HttpStatusCode
import io.ktor.http.isSuccess
import org.koin.test.get

class AccountRoutingKtTest : IntegrationTest() {
	private val eventStoreWriteClient: EventStoreWriteClient by lazy { get() }

	init {

		"should create new account" { client ->
			// arrange & act
			val response = client.createAccount(aCreateAccountRequest())

			// assert
			response.status shouldBe HttpStatusCode.OK
		}

		"should respond with 200 even when account already exists" { client ->
			// arrange
			val request = aCreateAccountRequest()
			val existingAccountRequest = aCreateAccountRequest(email = request.email)
			client.createAccount(request)

			// act
			val response = client.createAccount(existingAccountRequest)

			// assert
			response.status shouldBe HttpStatusCode.OK
		}

		"should respond with 400 on invalid create account request" { client ->
			// arrange
			val invalidRequest =
				aCreateAccountRequest(
					email = "invalid-mail",
					password = "123",
					timeZone = "invalid",
				)

			// act
			val response = client.createAccount(invalidRequest)

			// assert
			response.status shouldBe HttpStatusCode.BadRequest
			response.body<ValidationErrorHttpMessage>().validationErrors shouldHaveSize 3
		}

		"should activate account" { client ->
			// arrange
			val createAccountRequest = aCreateAccountRequest()
			client.createAccount(createAccountRequest)
			val token = getActivationToken(EmailAddress(createAccountRequest.email).getOrThrow())
			val activateAccountRequest = ActivateAccountRequest(token)

			// act
			val response = client.activateAccount(activateAccountRequest)

			// assert
			response.status shouldBe HttpStatusCode.OK
		}

		"should reset password" { client ->
			// arrange
			val createAccountRequest = aCreateAccountRequest()
			val updatedPassword = aRawPassword()
			client.createAndActivateAccount(createAccountRequest)
			val forgotPasswordRequest = ForgotPasswordRequest(createAccountRequest.email)

			client.forgotPassword(forgotPasswordRequest).status.isSuccess().shouldBeTrue()

			val resetPasswordToken = getResetPasswordToken(EmailAddress(createAccountRequest.email).getOrThrow())

			// act
			val response =
				client.resetPassword(
					ResetPasswordRequest(
						resetPasswordToken,
						MaskedString(updatedPassword.value),
					),
				)

			// assert
			response.status.isSuccess().shouldBeTrue()

			// and should log in using updated password
			client.logIn(LogInAccountRequest(createAccountRequest.email, MaskedString(updatedPassword.value)))
				.status.isSuccess().shouldBeTrue()
		}

		"should change password" { client ->
			// arrange
			val createAccountRequest = aCreateAccountRequest()
			val updatedPassword = aRawPassword()
			client.createAndActivateAccount(createAccountRequest)
			val authToken =
				client.getAuthToken(LogInAccountRequest(createAccountRequest.email, createAccountRequest.password))

			val changePasswordRequest =
				ChangePasswordRequest(
					createAccountRequest.password,
					MaskedString(updatedPassword.value),
				)

			// act
			val response = client.changePassword(changePasswordRequest, authToken)

			// assert
			response.status.isSuccess().shouldBeTrue()

			// and should log in using updated password
			client.logIn(LogInAccountRequest(createAccountRequest.email, MaskedString(updatedPassword.value)))
				.status.isSuccess().shouldBeTrue()
		}

		"should invite world creator" { client ->
			// arrange
			val createAccountRequest = aCreateAccountRequest()
			createSuperUserAccount(createAccountRequest)
			val request = InviteWorldCreatorRequest(anEmail().value)
			val authToken =
				client.getAuthToken(LogInAccountRequest(createAccountRequest.email, createAccountRequest.password))

			// act
			val response = client.inviteWorldCreator(request, authToken)

			// assert
			response.status.isSuccess().shouldBeTrue()
		}

		"should create world creator account" { client ->
			// arrange
			val email = anEmail()
			val password = aRawPassword()
			val token = client.inviteWorldCreatorReturningToken(email)
			val request =
				aCreateWorldCreatorAccountRequest(
					email = email.value,
					password = password.value,
					token = token,
				)

			// act
			val response = client.createWorldCreatorAccount(request)

			// assert
			response.status.isSuccess().shouldBeTrue()
		}

		"should resend activation mail" { client ->
			// arrange
			val createSuperUserAccountRequest = aCreateAccountRequest()
			createSuperUserAccount(createSuperUserAccountRequest)
			val superUserAuthToken =
				client.getAuthToken(
					LogInAccountRequest(createSuperUserAccountRequest.email, createSuperUserAccountRequest.password),
				)
			val createAccountRequest = aCreateAccountRequest()
			client.createAccount(createAccountRequest).status.isSuccess().shouldBeTrue()

			val resendActivationMailRequest = ResendActivationMailRequest(createAccountRequest.email)

			// act
			val response = client.resendActivationMail(resendActivationMailRequest, superUserAuthToken)

			// assert
			response.status.isSuccess().shouldBeTrue()
		}

		"should refresh auth token" { client ->
			// arrange
			val createAccountRequest = aCreateAccountRequest()
			client.createAndActivateAccount(createAccountRequest)
			val loginResponse =
				client.logIn(LogInAccountRequest(createAccountRequest.email, createAccountRequest.password))
					.body<LogInAccountResponse>()

			val request = RefreshTokenRequest(loginResponse.refreshToken, createAccountRequest.email)

			// act
			val response = client.refreshToken(request)

			// assert
			response.status.isSuccess().shouldBeTrue()
		}

		"should return info about authorized account" { client ->
			// arrange
			// arrange
			val createAccountRequest = aCreateAccountRequest()
			client.createAndActivateAccount(createAccountRequest)
			val authToken =
				client.getAuthToken(LogInAccountRequest(createAccountRequest.email, createAccountRequest.password))

			// act
			val response = client.me(authToken)

			// assert
			response.status.isSuccess().shouldBeTrue()
			response.body<AccountDetailsResponse>().should {
				it.email shouldBe createAccountRequest.email
				it.role shouldBe Roles.RegularUser.role
			}
		}
	}

	private suspend fun HttpClient.createAndActivateAccount(createAccountRequest: CreateAccountRequest) {
		createAccount(createAccountRequest).status.isSuccess().shouldBeTrue()
		val token = getActivationToken(EmailAddress(createAccountRequest.email).getOrThrow())
		val activateAccountRequest = ActivateAccountRequest(token)
		activateAccount(activateAccountRequest).status.isSuccess().shouldBeTrue()
	}

	private suspend fun HttpClient.getAuthToken(logInAccountRequest: LogInAccountRequest): JwtToken {
		val response = logIn(logInAccountRequest)
		response.status.isSuccess().shouldBeTrue()
		return JwtToken(response.body<LogInAccountResponse>().authToken)
	}

	private suspend fun createSuperUserAccount(createAccountRequest: CreateAccountRequest): AccountCreatedEvent {
		return anAccountCreatedEvent(
			email = EmailAddress(createAccountRequest.email).getOrThrow(),
			password = aRawPassword(createAccountRequest.password.value),
			status = AccountStatus.Active,
			role = Roles.SuperUser.role,
		).also { eventStoreWriteClient.appendToStream<AccountEvent>(it) }
	}

	private suspend fun HttpClient.inviteWorldCreatorReturningToken(emailAddress: EmailAddress): JwtToken {
		val createSuperUserRequest = aCreateAccountRequest()
		createSuperUserAccount(createSuperUserRequest)
		val superUserAuthToken =
			getAuthToken(LogInAccountRequest(createSuperUserRequest.email, createSuperUserRequest.password))
		val request = InviteWorldCreatorRequest(emailAddress.value)
		inviteWorldCreator(request, superUserAuthToken).status.isSuccess().shouldBeTrue()

		return JwtToken(getRegisterWorldCreatorToken(emailAddress))
	}
}
