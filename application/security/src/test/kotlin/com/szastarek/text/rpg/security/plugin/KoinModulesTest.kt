package com.szastarek.text.rpg.security.plugin

import io.kotest.core.extensions.Extension
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.koin.KoinExtension
import org.koin.test.KoinTest
import org.koin.test.check.checkModules

class KoinModulesTest : KoinTest, DescribeSpec() {

    override fun extensions(): List<Extension> {
        return listOf(KoinExtension(securityModule))
    }

    init {

        describe("Security Koin module test") {

            it("verify shared module") {
                //arrange & act & assert
                getKoin().checkModules()
            }
        }
    }

}
