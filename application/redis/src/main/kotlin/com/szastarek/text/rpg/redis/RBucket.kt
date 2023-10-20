package com.szastarek.text.rpg.redis

import arrow.core.Option
import org.redisson.api.RBucket

fun <T> RBucket<T>.getNullableAndDelete(): Option<T> {
  return Option.fromNullable(this.andDelete)
}