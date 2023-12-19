package com.szastarek.text.rpg.account.adapter.redis

import arrow.core.Option
import com.szastarek.text.rpg.account.RefreshToken
import com.szastarek.text.rpg.account.RefreshTokenRepository
import com.szastarek.text.rpg.monitoring.execute
import com.szastarek.text.rpg.redis.getNullableAndDelete
import com.szastarek.text.rpg.shared.email.EmailAddress
import io.opentelemetry.api.OpenTelemetry
import org.redisson.api.RedissonClient
import java.time.Duration

class RefreshTokenRedisRepository(
	private val redisClient: RedissonClient,
	private val openTelemetry: OpenTelemetry,
) : RefreshTokenRepository {
	override suspend fun getAndDelete(accountEmail: EmailAddress): Option<RefreshToken> {
		val tracer = openTelemetry.getTracer("redis")
		return tracer.spanBuilder("redis-get-refresh-token")
			.startSpan()
			.execute {
				redisClient.getBucket<String>(getKey(accountEmail)).getNullableAndDelete().map { RefreshToken(it) }
			}
	}

	override suspend fun replace(
		accountEmail: EmailAddress,
		token: RefreshToken,
	): RefreshToken {
		val tracer = openTelemetry.getTracer("redis")
		return tracer.spanBuilder("redis-replace-refresh-token")
			.startSpan()
			.execute {
				redisClient.getBucket<String>(getKey(accountEmail))[token.value] = refresh_token_ttl
				token
			}
	}

	private fun getKey(accountEmail: EmailAddress) = "refresh_token_${accountEmail.value}"

	companion object {
		private val refresh_token_ttl = Duration.ofDays(7)
	}
}
