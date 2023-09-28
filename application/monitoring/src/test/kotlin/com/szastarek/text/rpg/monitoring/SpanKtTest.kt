package com.szastarek.text.rpg.monitoring

import com.szastarek.text.rpg.utils.InMemoryOpenTelemetry
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.opentelemetry.api.trace.StatusCode
import java.lang.IllegalStateException

class SpanKtTest : DescribeSpec({

    val openTelemetry = InMemoryOpenTelemetry()

    beforeTest {
        openTelemetry.reset()
    }

    describe("SpanKtTest") {

        it("should execute code within new span") {
            //arrange
            val tracer = openTelemetry.get().tracerProvider.get("test-tracer")

            //act
            tracer.spanBuilder("test-span")
                .startSpan()
                .execute {
                    //some code executed within that span
                }

            //assert
            openTelemetry.getFinishedSpans() shouldHaveSize 1
            openTelemetry.getFinishedSpans().first().name shouldBe "test-span"
        }

        it("should create nested spans") {
            //arrange
            val tracer = openTelemetry.get().tracerProvider.get("test-tracer")

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
            openTelemetry.getFinishedSpans() shouldHaveSize 2
            openTelemetry.getFinishedSpans()[0].name shouldBe "nested-span"
            openTelemetry.getFinishedSpans()[0].parentSpanId shouldBe openTelemetry.getFinishedSpans()[1].spanId
            openTelemetry.getFinishedSpans()[1].name shouldBe "test-span"
        }

        it("should mark span as error when exception is thrown") {
            //arrange
            val tracer = openTelemetry.get().tracerProvider.get("test-tracer")

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
            openTelemetry.getFinishedSpans() shouldHaveSize 1
            openTelemetry.getFinishedSpans().first().status.statusCode shouldBe StatusCode.ERROR
        }
    }
})
