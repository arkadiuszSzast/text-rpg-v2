package com.szastarek.text.rpg.account.adapter.rest

import com.szastarek.text.rpg.account.support.IntegrationTest
import com.szastarek.text.rpg.account.support.createAccount
import io.ktor.client.statement.bodyAsText

class AccountRoutingKtTest : IntegrationTest() {

    init {

        describe("GET /v1") {

            it("should return 200") {
                val res = client.createAccount()
                val body = res.bodyAsText()
                println(body)
            }
        }
    }
}
