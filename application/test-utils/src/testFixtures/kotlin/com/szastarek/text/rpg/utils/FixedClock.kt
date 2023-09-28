package com.szastarek.text.rpg.utils

import kotlinx.datetime.Clock
import kotlinx.datetime.Instant

class FixedClock(@Volatile private var now: Instant = Instant.DISTANT_PAST) : Clock {
//
//  @Volatile
//  private var now = Instant.DISTANT_PAST

  fun setTo(value: Instant) {
    now = value
  }

  override fun now(): Instant {
    return now
  }
}