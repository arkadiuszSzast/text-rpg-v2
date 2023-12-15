package com.szastarek.text.rpg.redis

import com.szastarek.text.rpg.redis.plugin.configureKoin
import io.ktor.server.application.Application

fun Application.redisModule() {
	configureKoin()
}
