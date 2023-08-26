package com.szastarek.text.rpg.monitoring.config

import com.szastarek.text.rpg.monitoring.plugin.monitoringModule
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.koin.KoinExtension
import io.kotest.matchers.shouldBe
import org.koin.test.KoinTest
import org.koin.test.inject

class MonitoringPropertiesTest : KoinTest, DescribeSpec() {

    private val monitoringProperties by inject<MonitoringProperties>()

    init {

        extensions(KoinExtension(monitoringModule))

        describe("MonitoringPropertiesTest") {

            it("should pick correct values from application.conf") {
                //arrange
                val expected = MonitoringProperties(
                    enabled = true,
                    otelMetricsUrl = "http://test-host:4318/v1/metrics"
                )

                //act & assert
                monitoringProperties shouldBe expected
            }
        }
    }
}
