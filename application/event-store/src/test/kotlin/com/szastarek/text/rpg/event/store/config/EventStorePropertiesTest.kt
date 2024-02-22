package com.szastarek.text.rpg.event.store.config

import com.szastarek.text.rpg.event.store.plugin.eventStoreModule
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.koin.KoinExtension
import io.kotest.matchers.shouldBe
import org.koin.test.KoinTest
import org.koin.test.inject

class EventStorePropertiesTest : KoinTest, DescribeSpec() {
	private val eventStoreProperties by inject<EventStoreProperties>()

	init {

		extensions(KoinExtension(eventStoreModule))

		describe("EventStorePropertiesTest") {

			it("should pick correct values from application.conf") {
				// arrange
				val expected =
					EventStoreProperties(
						connectionString = "esdb://test-host:2113?tls=false",
						reSubscribeOnDrop = false,
					)

				// act & assert
				eventStoreProperties shouldBe expected
			}
		}
	}
}
