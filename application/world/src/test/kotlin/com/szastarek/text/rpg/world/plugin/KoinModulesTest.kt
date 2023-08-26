package com.szastarek.text.rpg.world.plugin

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.koin.KoinExtension
import org.koin.test.KoinTest
import org.koin.test.check.checkModules

class KoinModulesTest : KoinTest, DescribeSpec() {

    init {

        extensions(KoinExtension(worldModule))

        describe("World Koin module test") {

            it("verify world module") {
                //arrange & act & assert
                getKoin().checkModules()
            }
        }
    }
}
