package com.szastarek.text.rpg.account.adapter.rest

import com.szastarek.text.rpg.account.support.IntegrationTest
import com.szastarek.text.rpg.account.support.helloWorld
import io.kotest.matchers.shouldBe

class AccountRoutingKtTest : IntegrationTest() {

    init {

        describe("GET /v1") {

            it("should return 200") {
                client.helloWorld().status.value shouldBe 200
            }
        }
    }
}
