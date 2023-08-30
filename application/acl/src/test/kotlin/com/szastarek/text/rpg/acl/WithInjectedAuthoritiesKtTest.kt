package com.szastarek.text.rpg.acl

import com.szastarek.text.rpg.acl.authority.FeatureAccessAuthority
import com.szastarek.text.rpg.acl.authority.authorities
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.currentCoroutineContext

class WithInjectedAuthoritiesKtTest : DescribeSpec({

    describe("WithInjectedAuthorities") {

        it("should use injected authorities") {
            //arrange & act
            withInjectedAuthorities(authorities { featureAccess(Feature("some-feature")) }) {
                val expected = listOf(FeatureAccessAuthority(Feature("some-feature")))

                //assert
                currentCoroutineContext()[CoroutineInjectedAuthorityContext]?.authorities shouldBe expected
            }
        }

        it("should be able to use injected authorities in nested blocks") {
            //arrange & act
            withInjectedAuthorities(authorities { featureAccess(Feature("some-feature")) }) {
                withInjectedAuthorities(authorities { featureAccess(Feature("some-other-feature")) }) {
                    val expected = listOf(
                        FeatureAccessAuthority(Feature("some-feature")),
                        FeatureAccessAuthority(Feature("some-other-feature"))
                    )

                    //assert
                    currentCoroutineContext()[CoroutineInjectedAuthorityContext]?.authorities shouldContainExactlyInAnyOrder expected
                }
            }
        }

        it("same injected authorities should be distinct") {
            //arrange & act
            withInjectedAuthorities(authorities { featureAccess(Feature("some-feature")) }) {
                withInjectedAuthorities(authorities { featureAccess(Feature("some-feature")) }) {
                    val expected = listOf(FeatureAccessAuthority(Feature("some-feature")))

                    //assert
                    currentCoroutineContext()[CoroutineInjectedAuthorityContext]?.authorities shouldBe expected
                }
            }
        }

        it("when authorities are not injected then context should be null") {
            //arrange & act & assert
            currentCoroutineContext()[CoroutineInjectedAuthorityContext]?.authorities.shouldBeNull()
        }
    }
})
