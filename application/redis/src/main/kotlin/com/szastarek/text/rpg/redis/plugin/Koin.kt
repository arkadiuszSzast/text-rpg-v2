package com.szastarek.text.rpg.redis.plugin

import com.szastarek.text.rpg.redis.config.RedisProperties
import com.szastarek.text.rpg.shared.config.ConfigKey
import com.szastarek.text.rpg.shared.config.getStringProperty
import io.ktor.server.application.Application
import org.koin.core.context.GlobalContext.loadKoinModules
import org.koin.dsl.module
import org.redisson.Redisson
import org.redisson.config.Config

internal val redisModule =
	module {
		single { RedisProperties(getStringProperty(ConfigKey("redis.connectionString"))) }
		single {
			Redisson.create(
				Config().apply {
					useSingleServer().setAddress(get<RedisProperties>().connectionString)
				},
			)
		}
	}

internal fun Application.configureKoin() {
	loadKoinModules(redisModule)
}
