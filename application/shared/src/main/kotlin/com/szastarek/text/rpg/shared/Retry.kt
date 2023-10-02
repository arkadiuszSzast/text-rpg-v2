package com.szastarek.text.rpg.shared

import kotlinx.coroutines.delay

suspend fun <T> retry(maxAttempt: Long = 10, delayMs: Long = 100, attempt: Long = 0, block: suspend () -> T) {
  try {
    block()
  } catch (ex: Throwable) {
    if(attempt + 1 > maxAttempt) {
      throw ex
    }
    delay(delayMs)
    retry(maxAttempt, delayMs, attempt + 1, block)
  }
}