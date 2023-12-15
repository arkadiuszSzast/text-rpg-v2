package com.szastarek.text.rpg.redis

import org.testcontainers.containers.GenericContainer
import org.testcontainers.containers.wait.strategy.Wait

private const val REDIS_PORT = 6379

object RedisContainer {
	private val instance by lazy { startRedisContainer() }

	private val host: String
		get() = instance.host
	private val port: Int
		get() = instance.getMappedPort(REDIS_PORT)

	val connectionString: String
		get() = "redis://$host:$port"

	private fun startRedisContainer() =
		GenericContainer("redis/redis-stack:7.2.0-v3")
			.withExposedPorts(REDIS_PORT)
			.apply {
				setWaitStrategy(Wait.forListeningPort())
				start()
			}
}
