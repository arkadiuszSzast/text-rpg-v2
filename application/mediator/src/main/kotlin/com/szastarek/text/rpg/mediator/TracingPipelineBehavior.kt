package com.szastarek.text.rpg.mediator

import com.szastarek.text.rpg.monitoring.execute
import com.trendyol.kediatr.PipelineBehavior
import io.github.oshai.kotlinlogging.KotlinLogging
import io.opentelemetry.api.trace.TracerProvider
import org.litote.kmongo.newId

class TracingPipelineBehavior(private val tracerProvider: TracerProvider) : PipelineBehavior {
    private val logger = KotlinLogging.logger {}
    override suspend fun <TRequest, TResponse> handle(
        request: TRequest,
        next: suspend (TRequest) -> TResponse
    ): TResponse {
        val tracer = tracerProvider["mediator"]
        val requestType = KediatrRequestTypeExtractor.extract(request).code
        val requestSimpleName = request?.let { it::class.simpleName } ?: "not-known-request"
        val requestId = newId<TRequest>()

        return tracer.spanBuilder(requestSimpleName)
            .setAttribute(requestType, requestSimpleName)
            .setAttribute("requestId", requestId.toString())
            .startSpan()
            .execute {
                try {
                    logger.debug { "Executing $requestType $requestSimpleName [$requestId]. Payload: [$request]" }
                    val result = next(request)
                    logger.debug { "$requestType: $requestSimpleName [$requestId] executed successfully. Result: $result" }
                    result
                } catch (e: Throwable) {
                    logger.error(e) { "Error while executing $requestType $requestSimpleName [$requestId]." }
                    throw e
                }
            }
    }
}
