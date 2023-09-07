package com.szastarek.text.rpg.mediator

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.maps.shouldContain
import io.kotest.matchers.maps.shouldContainKey
import io.kotest.matchers.shouldBe
import io.opentelemetry.api.common.AttributeKey
import io.opentelemetry.api.trace.StatusCode
import io.opentelemetry.sdk.OpenTelemetrySdk
import io.opentelemetry.sdk.testing.exporter.InMemorySpanExporter
import io.opentelemetry.sdk.trace.SdkTracerProvider
import io.opentelemetry.sdk.trace.export.SimpleSpanProcessor

class TracingPipelineBehaviorTest : DescribeSpec({

    val spanExporter = InMemorySpanExporter.create()
    val tracerProvider = SdkTracerProvider
        .builder()
        .addSpanProcessor(SimpleSpanProcessor.create(spanExporter))
        .build()
    val openTelemetry = OpenTelemetrySdk.builder().setTracerProvider(tracerProvider).build()

    val tracingPipelineBehaviour = TracingPipelineBehavior(openTelemetry)

    describe("TracingPipelineBehaviorTest") {

        beforeTest {
            spanExporter.reset()
        }

        it("should execute Command within new span") {
            //arrange && act
            tracingPipelineBehaviour.handle(SimpleCommand()) {
                //simple command handler
            }

            //assert
            spanExporter.finishedSpanItems shouldHaveSize 1

            val span = spanExporter.finishedSpanItems.single()
            span.name shouldBe "SimpleCommand"
            span.attributes.asMap() shouldContain Pair(AttributeKey.stringKey("command"), "SimpleCommand")
            span.attributes.asMap() shouldContainKey AttributeKey.stringKey("requestId")
        }

        it("should execute CommandWithResult within new span") {
            //arrange && act
            tracingPipelineBehaviour.handle(SimpleCommandWithResult()) {
                //simple command with result handler
            }

            //assert
            spanExporter.finishedSpanItems shouldHaveSize 1

            val span = spanExporter.finishedSpanItems.single()
            span.name shouldBe "SimpleCommandWithResult"
            span.attributes.asMap() shouldContain Pair(AttributeKey.stringKey("command-with-result"), "SimpleCommandWithResult")
            span.attributes.asMap() shouldContainKey AttributeKey.stringKey("requestId")
        }

        it("should execute Notification within new span") {
            //arrange && act
            tracingPipelineBehaviour.handle(SimpleNotification()) {
                //simple notification handler
            }

            //assert
            spanExporter.finishedSpanItems shouldHaveSize 1

            val span = spanExporter.finishedSpanItems.single()
            span.name shouldBe "SimpleNotification"
            span.attributes.asMap() shouldContain Pair(AttributeKey.stringKey("notification"), "SimpleNotification")
            span.attributes.asMap() shouldContainKey AttributeKey.stringKey("requestId")
        }

        it("should execute Query within new span") {
            //arrange && act
            tracingPipelineBehaviour.handle(SimpleQuery()) {
                //simple query handler
            }

            //assert
            spanExporter.finishedSpanItems shouldHaveSize 1

            val span = spanExporter.finishedSpanItems.single()
            span.name shouldBe "SimpleQuery"
            span.attributes.asMap() shouldContain Pair(AttributeKey.stringKey("query"), "SimpleQuery")
            span.attributes.asMap() shouldContainKey AttributeKey.stringKey("requestId")
        }

        it("should rethrow exception and mark span as error") {
            //arrange && act && assert
            shouldThrow<IllegalArgumentException> {
                tracingPipelineBehaviour.handle(SimpleCommand()) {
                    throw IllegalArgumentException("test exception")
                }
            }

            spanExporter.finishedSpanItems shouldHaveSize 1

            val span = spanExporter.finishedSpanItems.single()
            span.name shouldBe "SimpleCommand"
            span.attributes.asMap() shouldContain Pair(AttributeKey.stringKey("command"), "SimpleCommand")
            span.attributes.asMap() shouldContainKey AttributeKey.stringKey("requestId")
            span.status.statusCode shouldBe StatusCode.ERROR
        }
    }
})
