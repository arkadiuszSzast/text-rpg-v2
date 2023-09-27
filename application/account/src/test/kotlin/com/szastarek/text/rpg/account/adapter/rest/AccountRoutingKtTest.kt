package com.szastarek.text.rpg.account.adapter.rest

import com.szastarek.text.rpg.account.support.IntegrationTest
import com.szastarek.text.rpg.account.support.aCreateAccountRequest
import com.szastarek.text.rpg.account.support.createAccount
import com.szastarek.text.rpg.shared.ValidationErrorHttpMessage
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.ktor.client.call.*
import io.ktor.http.*

class AccountRoutingKtTest : IntegrationTest() {

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

            it("should respond with 400 on invalid request") {
                //arrange
                val invalidRequest = aCreateAccountRequest(email = "invalid-mail", password = "123", timeZone = "invalid")

                //act
                val response = client.createAccount(invalidRequest)

                //assert
                response.status shouldBe HttpStatusCode.BadRequest
                response.body<ValidationErrorHttpMessage>().validationErrors shouldHaveSize 3
            }
        }
    }
}
