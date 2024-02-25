package com.szastarek.text.rpg.account.plugin

import com.eventstore.dbclient.EventStoreDBClient
import com.szastarek.text.rpg.shared.plugin.HealthzPlugin
import io.ktor.server.application.Application
import io.ktor.server.application.install
import kotlinx.coroutines.future.await
import org.redisson.api.RedissonClient
import org.redisson.api.redisnode.RedisNodes

internal fun Application.configureHealthz(eventStoreDBClient: EventStoreDBClient, redisClient: RedissonClient) {
	install(HealthzPlugin) {
		healthChecks {
			check("main") { true }
			check("eventstore") { eventStoreDBClient.serverVersion.await().isPresent }
			check("redis") { redisClient.getRedisNodes(RedisNodes.CLUSTER).pingAll() }
		}
		readyChecks {
			check("main") { true }
			check("eventstore") { eventStoreDBClient.serverVersion.await().isPresent }
			check("redis") { redisClient.getRedisNodes(RedisNodes.CLUSTER).pingAll() }
		}
	}
}
