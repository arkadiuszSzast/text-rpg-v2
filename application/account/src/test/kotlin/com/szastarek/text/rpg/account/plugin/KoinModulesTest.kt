package com.szastarek.text.rpg.account.plugin

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.koin.KoinExtension
import org.koin.test.KoinTest
import org.koin.test.check.checkModules

class KoinModulesTest : KoinTest, DescribeSpec() {

    init {

        extensions(KoinExtension(accountModule))

        describe("Account Koin module test") {

            it("verify account module") {
                //arrange & act & assert
                getKoin().checkModules()
            }
        }
    }
}
