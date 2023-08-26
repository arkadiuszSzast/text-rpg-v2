package com.szastarek.text.rpg.shared.plugin

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.koin.KoinExtension
import org.koin.test.KoinTest
import org.koin.test.check.checkModules

class KoinModulesTest : KoinTest, DescribeSpec() {

    init {

        extensions(KoinExtension(sharedKoinModule))

        describe("Shared Koin module test") {

            it("verify shared module") {
                //arrange & act & assert
                getKoin().checkModules()
            }
        }
    }
}
