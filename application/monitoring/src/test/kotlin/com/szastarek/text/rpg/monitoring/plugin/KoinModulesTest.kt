package com.szastarek.text.rpg.monitoring.plugin

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.koin.KoinExtension
import org.koin.test.KoinTest
import org.koin.test.check.checkModules

class KoinModulesTest : KoinTest, DescribeSpec() {
	init {

		extensions(KoinExtension(monitoringModule))

		describe("Monitoring Koin module test") {

			it("verify monitoring module") {
				// arrange & act & assert
				getKoin().checkModules()
			}
		}
	}
}
