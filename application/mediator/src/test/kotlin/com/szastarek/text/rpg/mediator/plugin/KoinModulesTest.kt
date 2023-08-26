package com.szastarek.text.rpg.mediator.plugin

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.koin.KoinExtension
import org.koin.test.KoinTest
import org.koin.test.check.checkModules

class KoinModulesTest : KoinTest, DescribeSpec() {

    init {

        extensions(KoinExtension(mediatorKoinModule))

        describe("Mediator Koin module test") {

            it("verify mediator module") {
                //arrange & act & assert
                getKoin().checkModules()
            }
        }
    }
}
