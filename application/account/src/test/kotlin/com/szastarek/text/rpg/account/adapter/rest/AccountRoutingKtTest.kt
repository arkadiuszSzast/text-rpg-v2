package com.szastarek.text.rpg.account.adapter.rest

import com.szastarek.text.rpg.account.AccountStatus
import com.szastarek.text.rpg.account.adapter.rest.request.*
import com.szastarek.text.rpg.account.adapter.rest.response.AccountDetailsResponse
import com.szastarek.text.rpg.account.adapter.rest.response.LogInAccountResponse
import com.szastarek.text.rpg.account.event.AccountCreatedEvent
import com.szastarek.text.rpg.account.event.AccountEvent
import com.szastarek.text.rpg.account.support.*
import com.szastarek.text.rpg.account.support.activateAccount
import com.szastarek.text.rpg.account.support.createAccount
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
import io.ktor.client.call.*
import io.ktor.http.*
import org.koin.test.inject

class AccountRoutingKtTest : IntegrationTest() {

  private val eventStoreWriteClient by inject<EventStoreWriteClient>()

  init {

    describe("AccountRoutingTest") {

      it("should create new account") {
        //arrange & act
        val response = client.createAccount(aCreateAccountRequest())

        //assert
        response.status shouldBe HttpStatusCode.OK
      }

      it("should respond with 200 even when account already exists") {
        //arrange
        val request = aCreateAccountRequest()
        val existingAccountRequest = aCreateAccountRequest(email = request.email)
        client.createAccount(request)

        //act
        val response = client.createAccount(existingAccountRequest)

        //assert
        response.status shouldBe HttpStatusCode.OK
      }

      it("should respond with 400 on invalid create account request") {
        //arrange
        val invalidRequest = aCreateAccountRequest(email = "invalid-mail", password = "123", timeZone = "invalid")

        //act
        val response = client.createAccount(invalidRequest)

        //assert
        response.status shouldBe HttpStatusCode.BadRequest
        response.body<ValidationErrorHttpMessage>().validationErrors shouldHaveSize 3
      }

      it("should activate account") {
        //arrange
        val createAccountRequest = aCreateAccountRequest()
        client.createAccount(createAccountRequest)
        val token = getActivationToken(EmailAddress(createAccountRequest.email).getOrThrow())
        val activateAccountRequest = ActivateAccountRequest(token)

        //act
        val response = client.activateAccount(activateAccountRequest)

        //assert
        response.status shouldBe HttpStatusCode.OK
      }

      it("should reset password") {
        //arrange
        val createAccountRequest = aCreateAccountRequest()
        val updatedPassword = aRawPassword()
        createAndActivateAccount(createAccountRequest)
        val forgotPasswordRequest = ForgotPasswordRequest(createAccountRequest.email)

        client.forgotPassword(forgotPasswordRequest).status.isSuccess().shouldBeTrue()

        val resetPasswordToken = getResetPasswordToken(EmailAddress(createAccountRequest.email).getOrThrow())

        //act
        val response = client.resetPassword(
          ResetPasswordRequest(
            resetPasswordToken,
            MaskedString(updatedPassword.value)
          )
        )

        //assert
        response.status.isSuccess().shouldBeTrue()

        //and should log in using updated password
        client.logIn(LogInAccountRequest(createAccountRequest.email, MaskedString(updatedPassword.value)))
          .status.isSuccess().shouldBeTrue()
      }

      it("should change password") {
        //arrange
        val createAccountRequest = aCreateAccountRequest()
        val updatedPassword = aRawPassword()
        createAndActivateAccount(createAccountRequest)
        val authToken = getAuthToken(LogInAccountRequest(createAccountRequest.email, createAccountRequest.password))

        val changePasswordRequest = ChangePasswordRequest(
          createAccountRequest.password,
          MaskedString(updatedPassword.value)
        )

        //act
        val response = client.changePassword(changePasswordRequest, authToken)

        //assert
        response.status.isSuccess().shouldBeTrue()

        //and should log in using updated password
        client.logIn(LogInAccountRequest(createAccountRequest.email, MaskedString(updatedPassword.value)))
          .status.isSuccess().shouldBeTrue()
      }

      it("should invite world creator") {
        //arrange
        val createAccountRequest = aCreateAccountRequest()
        createSuperUserAccount(createAccountRequest)
        val request = InviteWorldCreatorRequest(anEmail().value)
        val authToken = getAuthToken(LogInAccountRequest(createAccountRequest.email, createAccountRequest.password))

        //act
        val response = client.inviteWorldCreator(request, authToken)

        //assert
        response.status.isSuccess().shouldBeTrue()
      }

      it("should create world creator account") {
        //arrange
        val email = anEmail()
        val password = aRawPassword()
        val token = inviteWorldCreatorReturningToken(email)
        val request = aCreateWorldCreatorAccountRequest(
          email = email.value,
          password = password.value,
          token = token
        )

        //act
        val response = client.createWorldCreatorAccount(request)

        //assert
        response.status.isSuccess().shouldBeTrue()
      }

      it("should resend activation mail") {
        //arrange
        val createSuperUserAccountRequest = aCreateAccountRequest()
        createSuperUserAccount(createSuperUserAccountRequest)
        val superUserAuthToken = getAuthToken(
          LogInAccountRequest(createSuperUserAccountRequest.email, createSuperUserAccountRequest.password)
        )
        val createAccountRequest = aCreateAccountRequest()
        client.createAccount(createAccountRequest).status.isSuccess().shouldBeTrue()

        val resendActivationMailRequest = ResendActivationMailRequest(createAccountRequest.email)

        //act
        val response = client.resendActivationMail(resendActivationMailRequest, superUserAuthToken)

        //assert
        response.status.isSuccess().shouldBeTrue()
      }

      it("should refresh auth token") {
        //arrange
        val createAccountRequest = aCreateAccountRequest()
        createAndActivateAccount(createAccountRequest)
        val loginResponse = client.logIn(LogInAccountRequest(createAccountRequest.email, createAccountRequest.password))
          .body<LogInAccountResponse>()

        val request = RefreshTokenRequest(loginResponse.refreshToken, createAccountRequest.email)

        //act
        val response = client.refreshToken(request)

        //assert
        response.status.isSuccess().shouldBeTrue()
      }

      it("should return info about authorized account") {
       //arrange
        //arrange
        val createAccountRequest = aCreateAccountRequest()
        createAndActivateAccount(createAccountRequest)
        val authToken = getAuthToken(LogInAccountRequest(createAccountRequest.email, createAccountRequest.password))

        //act
        val response = client.me(authToken)

        //assert
        response.status.isSuccess().shouldBeTrue()
        response.body<AccountDetailsResponse>().should {
          it.email shouldBe createAccountRequest.email
          it.role shouldBe Roles.RegularUser.role
        }
      }
    }
  }

  private suspend fun createAndActivateAccount(createAccountRequest: CreateAccountRequest) {
    client.createAccount(createAccountRequest).status.isSuccess().shouldBeTrue()
    val token = getActivationToken(EmailAddress(createAccountRequest.email).getOrThrow())
    val activateAccountRequest = ActivateAccountRequest(token)
    client.activateAccount(activateAccountRequest).status.isSuccess().shouldBeTrue()
  }

  private suspend fun getAuthToken(logInAccountRequest: LogInAccountRequest): JwtToken {
    val response = client.logIn(logInAccountRequest)
    response.status.isSuccess().shouldBeTrue()
    return JwtToken(response.body<LogInAccountResponse>().authToken)
  }

  private suspend fun createSuperUserAccount(createAccountRequest: CreateAccountRequest): AccountCreatedEvent {
    return anAccountCreatedEvent(
      email = EmailAddress(createAccountRequest.email).getOrThrow(),
      password = aRawPassword(createAccountRequest.password.value),
      status = AccountStatus.Active,
      role = Roles.SuperUser.role
    ).also { eventStoreWriteClient.appendToStream<AccountEvent>(it) }
  }

  private suspend fun inviteWorldCreatorReturningToken(emailAddress: EmailAddress): JwtToken {
    val createSuperUserRequest = aCreateAccountRequest()
    createSuperUserAccount(createSuperUserRequest)
    val superUserAuthToken = getAuthToken(LogInAccountRequest(createSuperUserRequest.email, createSuperUserRequest.password))
    val request = InviteWorldCreatorRequest(emailAddress.value)
    client.inviteWorldCreator(request, superUserAuthToken).status.isSuccess().shouldBeTrue()

    return JwtToken(getRegisterWorldCreatorToken(emailAddress))
  }
}
