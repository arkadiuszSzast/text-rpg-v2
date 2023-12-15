package com.szastarek.text.rpg.redis.config

import com.szastarek.text.rpg.redis.plugin.redisModule
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.koin.KoinExtension
import io.kotest.matchers.shouldBe
import org.koin.test.KoinTest
import org.koin.test.inject

class RedisPropertiesTest : KoinTest, DescribeSpec() {
	private val redisProperties by inject<RedisProperties>()

	init {

		extensions(KoinExtension(redisModule))

		describe("RedisPropertiesTest") {

			it("should pick correct values from application.conf") {
				// arrange
				val expected =
					RedisProperties(
						connectionString = "redis://test-host:7181",
					)

				// act & assert
				redisProperties shouldBe expected
			}
		}
	}
}
