package com.szastarek.text.rpg.monitoring

import io.opentelemetry.api.trace.Span
import io.opentelemetry.api.trace.StatusCode
import io.opentelemetry.extension.kotlin.asContextElement
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.withContext

suspend inline fun <T> Span.execute(crossinline block: suspend () -> T): T =
	try {
		this.makeCurrent().use {
			withContext(currentCoroutineContext() + this.asContextElement()) {
				block()
			}
		}
	} catch (ex: Throwable) {
		setStatus(StatusCode.ERROR)
		recordException(ex)
		throw ex
	} finally {
		end()
	}
