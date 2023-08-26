package com.szastarek.text.rpg.monitoring

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.opentelemetry.api.trace.StatusCode
import io.opentelemetry.sdk.testing.exporter.InMemorySpanExporter
import io.opentelemetry.sdk.trace.SdkTracerProvider
import io.opentelemetry.sdk.trace.export.SimpleSpanProcessor
import java.lang.IllegalStateException

class SpanKtTest : DescribeSpec({

    val spanExporter = InMemorySpanExporter.create()
    val tracerProvider = SdkTracerProvider
        .builder()
        .addSpanProcessor(SimpleSpanProcessor.create(spanExporter))
        .build()


    beforeTest {
        spanExporter.reset()
    }

    describe("SpanKtTest") {

        it("should execute code within new span") {
            //arrange
            val tracer = tracerProvider.get("test-tracer")

            //act
            tracer.spanBuilder("test-span")
                .startSpan()
                .execute {
                    //some code executed within that span
                }

            //assert
            spanExporter.finishedSpanItems shouldHaveSize 1
            spanExporter.finishedSpanItems.first().name shouldBe "test-span"
        }

        it("should create nested spans") {
            //arrange
            val tracer = tracerProvider.get("test-tracer")

            //act
            tracer.spanBuilder("test-span")
                .startSpan()
                .execute {
                    //some code executed within that span

                    tracer.spanBuilder("nested-span")
                        .startSpan()
                        .execute {
                            //some code executed within nested span
                        }
                }

            //assert
            spanExporter.finishedSpanItems shouldHaveSize 2
            spanExporter.finishedSpanItems[0].name shouldBe "nested-span"
            spanExporter.finishedSpanItems[0].parentSpanId shouldBe spanExporter.finishedSpanItems[1].spanId
            spanExporter.finishedSpanItems[1].name shouldBe "test-span"
        }

        it("should mark span as error when exception is thrown") {
            //arrange
            val tracer = tracerProvider.get("test-tracer")

            //act
            shouldThrow<IllegalStateException> {
                tracer.spanBuilder("test-span")
                    .startSpan()
                    .execute {
                        //some code executed within that span
                        throw IllegalStateException("test exception")
                    }
            }

            //assert
            spanExporter.finishedSpanItems shouldHaveSize 1
            spanExporter.finishedSpanItems.first().status.statusCode shouldBe StatusCode.ERROR
        }
    }
})
