package com.szastarek.text.rpg.security.config

import com.szastarek.text.rpg.security.plugin.securityModule
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.koin.KoinExtension
import io.kotest.matchers.shouldBe
import org.koin.test.KoinTest
import org.koin.test.inject
import kotlin.time.Duration.Companion.milliseconds

class AuthenticationPropertiesTest : KoinTest, DescribeSpec() {

    private val authenticationProperties by inject<AuthenticationProperties>()

    init {

        extensions(KoinExtension(securityModule))

        describe("AuthenticationPropertiesTest") {

            it("should pick correct values from application.conf") {
                //arrange
                val expected = AuthenticationProperties(
                    jwtAudience = "test-audience",
                    jwtIssuer = "test-issuer",
                    jwtRealm = "test-realm",
                    jwtSecret = "test-secret",
                    authTokenExpiration = 10000.milliseconds
                )

                //act & assert
                authenticationProperties shouldBe expected
            }
        }
    }
}
