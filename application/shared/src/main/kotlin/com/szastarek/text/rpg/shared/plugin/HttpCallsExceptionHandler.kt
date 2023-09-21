package com.szastarek.text.rpg.shared.plugin

import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktor.server.application.ApplicationCall
import io.ktor.server.application.ApplicationCallPipeline
import io.ktor.server.application.BaseRouteScopedPlugin
import io.ktor.server.application.call
import io.ktor.server.request.uri
import io.ktor.util.AttributeKey
import io.ktor.util.pipeline.PipelinePhase
import io.ktor.util.reflect.instanceOf
import kotlin.reflect.KClass

typealias HandlerFunction = suspend (call: ApplicationCall, cause: Throwable) -> Unit

class HttpCallsExceptionHandler {

    class Configuration {
        val handlers: MutableMap<KClass<*>, suspend (call: ApplicationCall, cause: Throwable) -> Unit> = mutableMapOf()

        inline fun <reified T : Throwable> exception(noinline handler: suspend (call: ApplicationCall, cause: T) -> Unit) {
            @Suppress("UNCHECKED_CAST")
            handlers[T::class] = handler as suspend (ApplicationCall, Throwable) -> Unit
        }

    }

    companion object Plugin : BaseRouteScopedPlugin<Configuration, HttpCallsExceptionHandler> {
        private val logger = KotlinLogging.logger {}
        override val key: AttributeKey<HttpCallsExceptionHandler> = AttributeKey("HttpCallsExceptionHandler")

        override fun install(
            pipeline: ApplicationCallPipeline,
            configure: Configuration.() -> Unit
        ): HttpCallsExceptionHandler {
            val feature = HttpCallsExceptionHandler()
            val config = Configuration().apply(configure)
            val phase = PipelinePhase("HttpCallsExceptionHandler")
            pipeline.insertPhaseAfter(ApplicationCallPipeline.Plugins, phase)

            pipeline.intercept(phase) {
                try {
                    proceed()
                } catch (e: Throwable) {
                    val handler = config.handlers.keys.filter { e.instanceOf(it) }
                        .let { selectNearestParentClass(e, it) }
                        .let { config.handlers[it] }

                    if (handler == null) {
                        logger.debug { "No handler found for exception: ${e.javaClass.name} for call ${call.request.uri}" }
                        throw e
                    }
                    logger.error(e) { "Handling exception: ${e.javaClass.name} for call ${call.request.uri}" }
                    handler(call, e)
                }
            }

            return feature
        }
    }
}

private fun selectNearestParentClass(cause: Throwable, keys: List<KClass<*>>): KClass<*>? =
    keys.minByOrNull { distance(cause.javaClass, it.java) }

private fun distance(child: Class<*>, parent: Class<*>): Int {
    var result = 0
    var current = child
    while (current != parent) {
        current = current.superclass
        result++
    }

    return result
}
