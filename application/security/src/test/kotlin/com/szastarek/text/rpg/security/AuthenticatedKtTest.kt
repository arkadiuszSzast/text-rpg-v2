package com.szastarek.text.rpg.security

import com.szastarek.text.rpg.acl.AccountContext
import com.szastarek.text.rpg.acl.AccountId
import com.szastarek.text.rpg.acl.AnonymousAccountContext
import com.szastarek.text.rpg.acl.AuthenticatedAccountContext
import com.szastarek.text.rpg.acl.Feature
import com.szastarek.text.rpg.acl.Role
import com.szastarek.text.rpg.acl.Roles
import com.szastarek.text.rpg.acl.authority.Authority
import com.szastarek.text.rpg.acl.authority.authorities
import com.szastarek.text.rpg.acl.getAuthorities
import com.szastarek.text.rpg.shared.email.EmailAddress
import com.szastarek.text.rpg.shared.validate.getOrThrow
import io.kotest.core.spec.Spec
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.http.HttpStatusCode
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.install
import io.ktor.server.application.call
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.plugins.statuspages.StatusPages
import io.ktor.server.response.respond
import io.ktor.server.routing.get
import io.ktor.server.routing.routing
import io.ktor.server.testing.TestApplication
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.koin.ktor.plugin.Koin
import org.koin.test.KoinTest
import org.koin.test.inject
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation.Plugin as ClientContentNegotiation

class AuthenticatedKtTest : KoinTest, DescribeSpec() {

    private val testApplication = TestApplication {
        application {
            install(Koin)
            install(StatusPages) {
                exception<NotAuthenticatedException> { call, _ ->
                    call.respond(HttpStatusCode.Unauthorized)
                }
            }
            install(ContentNegotiation) {
                json(Json)
            }
            securityModule()
            routing {
                authenticated {
                    get("/me") {
                        call.respond(HttpStatusCode.OK, call.getAccountContext().toAccountInfoResponse())
                    }
                }
            }
        }
    }.also { it.start() }

    private val authTokenProvider by inject<AuthTokenProvider>()

    override fun afterSpec(f: suspend (Spec) -> Unit) {
        testApplication.stop()
    }

    init {

        describe("AuthenticatedKtTest") {

            it("should return authenticated account info") {
                //arrange
                val accountId = AccountId("test-account")
                val emailAddress = EmailAddress("test@mail.com").getOrThrow()
                val role = Roles.RegularUser.role
                val authorities = authorities { featureAccess(Feature("test-feature")) }
                val token = authTokenProvider.createAuthToken(accountId, emailAddress, role, authorities).value
                val expectedResponse = AccountInfoResponse(accountId, role, role.getAuthorities() + authorities)

                //act
                val response = testApplication.createClient {
                    install(ClientContentNegotiation) {
                        json(Json)
                    }
                }.get("/me") {
                    header("Authorization", "Bearer $token")
                }

                //assert
                response.status shouldBe HttpStatusCode.OK
                response.body<AccountInfoResponse>() shouldBe expectedResponse
            }

            it("should return 401 when token is not provided") {
                //arrange & act
                val response = testApplication.createClient {
                    install(ClientContentNegotiation) {
                        json(Json)
                    }
                }.get("/me")

                //assert
                response.status shouldBe HttpStatusCode.Unauthorized
            }
        }
    }
}

private suspend fun AccountContext.toAccountInfoResponse() =
    when (this) {
        is AuthenticatedAccountContext -> AccountInfoResponse(accountId, role, getAuthorities())
        is AnonymousAccountContext -> throw NotAuthenticatedException()
    }

@Serializable
private data class AccountInfoResponse(val accountId: AccountId, val role: Role, val authorities: List<Authority>)
