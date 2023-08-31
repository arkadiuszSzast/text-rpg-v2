package com.szastarek.text.rpg.shared.plugin

import com.szastarek.text.rpg.shared.ProblemHttpErrorResponse
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.http.HttpStatusCode
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.install
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.response.respond
import io.ktor.server.routing.get
import io.ktor.server.routing.routing
import io.ktor.server.testing.TestApplication
import kotlinx.serialization.json.Json
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation.Plugin as ClientContentNegotiation

class HttpCallsExceptionHandlerTest : DescribeSpec() {

    private val runtimeExceptionProblemResponse = ProblemHttpErrorResponse(
        "RuntimeException",
        "Something went wrong",
        "test-instance"
    )
    private val registeredExceptionExtendingRuntimeExceptionProblemResponse = ProblemHttpErrorResponse(
        "RegisteredExceptionExtendingRuntimeException",
        "Registered exception extending RuntimeException title",
        "test-instance"
    )

    private val testApplication = TestApplication {
        application {
            install(HttpCallsExceptionHandler) {
                exception<RuntimeException> { call, _ ->
                    call.respond(HttpStatusCode.NotImplemented, runtimeExceptionProblemResponse)
                }
                exception<RegisteredExceptionExtendingRuntimeException> { call, _ ->
                    call.respond(
                        HttpStatusCode.GatewayTimeout,
                        registeredExceptionExtendingRuntimeExceptionProblemResponse
                    )
                }
            }
            install(ContentNegotiation) {
                json(Json)
            }
            routing {
                get("/runtime-exception") {
                    throw RuntimeException()
                }
                get("/registered-exception-extending-runtime-exception") {
                    throw RegisteredExceptionExtendingRuntimeException()
                }
                get("/not-registered-exception-extending-runtime-exception") {
                    throw NotRegisteredExceptionExtendingRuntimeException()
                }
                get("/exception-not-registered-in-plugin") {
                    throw ExceptionNotRegisteredInPlugin()
                }
            }
        }
    }.also { it.start() }

    init {

        describe("HttpCallsExceptionHandlerTest") {

            it("should pick handler by direct exception type") {
                //arrange
                val expectedResponse = runtimeExceptionProblemResponse
                //act
                val response = testApplication.createClient {
                    expectSuccess = false
                    install(ClientContentNegotiation) {
                        json(Json)
                    }
                }.get("/runtime-exception")

                //assert
                response.status shouldBe HttpStatusCode.NotImplemented
                response.body<ProblemHttpErrorResponse>() shouldBe expectedResponse
            }

            it("should use runtime exception handler for NotRegisteredExceptionExtendingRuntimeException") {
                //arrange
                val expectedResponse = runtimeExceptionProblemResponse
                //act
                val response = testApplication.createClient {
                    install(ClientContentNegotiation) {
                        json(Json)
                    }
                }.get("/not-registered-exception-extending-runtime-exception")

                //assert
                response.status shouldBe HttpStatusCode.NotImplemented
                response.body<ProblemHttpErrorResponse>() shouldBe expectedResponse
            }

            it("should pick the most specific handler") {
                //arrange
                val expectedResponse = registeredExceptionExtendingRuntimeExceptionProblemResponse
                //act
                val response = testApplication.createClient {
                    install(ClientContentNegotiation) {
                        json(Json)
                    }
                }.get("/registered-exception-extending-runtime-exception")

                //assert
                response.status shouldBe HttpStatusCode.GatewayTimeout
                response.body<ProblemHttpErrorResponse>() shouldBe expectedResponse
            }

            it("should do nothing for exception that does not match any handler") {
                //arrange & act & assert
                shouldThrow<ExceptionNotRegisteredInPlugin> {
                    testApplication.client.get("/exception-not-registered-in-plugin")
                }
            }
        }
    }
}

private class NotRegisteredExceptionExtendingRuntimeException :
    RuntimeException("Not registered exception extending RuntimeException")

private class RegisteredExceptionExtendingRuntimeException :
    RuntimeException("Registered exception extending RuntimeException")

private class ExceptionNotRegisteredInPlugin : Throwable("Specific exception not registered in plugin")
