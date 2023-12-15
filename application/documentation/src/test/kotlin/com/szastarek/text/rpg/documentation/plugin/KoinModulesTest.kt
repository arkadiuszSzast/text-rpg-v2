package com.szastarek.text.rpg.documentation.plugin

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.koin.KoinExtension
import org.koin.test.KoinTest
import org.koin.test.check.checkModules

class KoinModulesTest : KoinTest, DescribeSpec() {
	init {

		extensions(KoinExtension(documentationModule))

		describe("Documentation Koin module test") {

			it("verify documentation module") {
				// arrange & act & assert
				getKoin().checkModules()
			}
		}
	}
}
