package com.szastarek.text.rpg.security.plugin

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.koin.KoinExtension
import org.koin.test.KoinTest
import org.koin.test.check.checkModules

class KoinModulesTest : KoinTest, DescribeSpec() {
	init {

		extensions(KoinExtension(securityModule))

		describe("Security Koin module test") {

			it("verify security module") {
				// arrange & act & assert
				getKoin().checkModules()
			}
		}
	}
}
