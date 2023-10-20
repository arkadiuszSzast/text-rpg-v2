package com.szastarek.text.rpg.account.adapter.redis

import com.szastarek.text.rpg.account.RefreshToken
import com.szastarek.text.rpg.redis.RedisContainer
import com.szastarek.text.rpg.shared.anEmail
import com.szastarek.text.rpg.utils.InMemoryOpenTelemetry
import io.kotest.assertions.arrow.core.shouldBeNone
import io.kotest.assertions.arrow.core.shouldBeSome
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.collections.shouldContainOnly
import io.kotest.matchers.collections.shouldHaveSize
import org.redisson.Redisson
import org.redisson.config.Config

class RefreshTokenRedisRepositoryTest : DescribeSpec() {

  private val redisClient = Redisson.create(Config().apply { useSingleServer().setAddress(RedisContainer.connectionString) })
  private val openTelemetry = InMemoryOpenTelemetry()
  private val refreshTokenRedisRepository = RefreshTokenRedisRepository(redisClient, openTelemetry.get())

  init {

    describe("RefreshTokenRedisRepositoryTest") {

      beforeTest {
        openTelemetry.reset()
        val redisKeys = redisClient.keys.keys
        redisClient.keys.delete(*redisKeys.toList().toTypedArray())
      }

      it("should get and delete refresh token") {
        //arrange
        val accountEmail = anEmail()
        val refreshToken = RefreshToken.generate()
        refreshTokenRedisRepository.replace(accountEmail, refreshToken)

        //act & assert
        refreshTokenRedisRepository.getAndDelete(accountEmail).shouldBeSome(refreshToken)

        //and token cannot be fetched second time
        refreshTokenRedisRepository.getAndDelete(accountEmail).shouldBeNone()
      }

      it("should replace refresh token") {
        //arrange
        val accountEmail = anEmail()
        val refreshToken = RefreshToken.generate()
        val latestRefreshToken = RefreshToken.generate()

        //initially should be empty
        refreshTokenRedisRepository.getAndDelete(accountEmail).shouldBeNone()

        //act
        refreshTokenRedisRepository.replace(accountEmail, refreshToken)
        refreshTokenRedisRepository.replace(accountEmail, latestRefreshToken)

        //assert
        refreshTokenRedisRepository.getAndDelete(accountEmail).shouldBeSome(latestRefreshToken)
      }

      it("should process in new span") {
        //arrange
        val accountEmail = anEmail()
        val refreshToken = RefreshToken.generate()

        //act
        refreshTokenRedisRepository.replace(accountEmail, refreshToken)
        refreshTokenRedisRepository.getAndDelete(accountEmail)

        //assert
        openTelemetry.getFinishedSpans().shouldHaveSize(2)
        openTelemetry.getFinishedSpans().map { it.name }
          .shouldContainOnly("redis-replace-refresh-token", "redis-get-refresh-token")
      }
    }
  }
}
