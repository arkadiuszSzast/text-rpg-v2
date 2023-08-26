package com.szastarek.text.rpg.shared.plugin

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.ktor.server.application.createApplicationPlugin
import io.ktor.server.testing.TestApplication
import java.util.concurrent.atomic.AtomicInteger

class PluginsKtTest : DescribeSpec({


    describe("Plugins extensions") {

        val installCounter = AtomicInteger(0)
        val testPlugin = createApplicationPlugin("stateful-plugin") {
            installCounter.incrementAndGet()
        }

        beforeTest {
            installCounter.set(0)
        }

        it("should install plugin") {
            //arrange && act
            TestApplication {
                application {
                    installIfNotRegistered(testPlugin)
                }
            }.start()

            //assert
            installCounter.get() shouldBe 1
        }

        it("should not install plugin if already registered") {
            //arrange && act
            TestApplication {
                application {
                    installIfNotRegistered(testPlugin)
                    installIfNotRegistered(testPlugin)
                }
            }.start()

            //assert
            installCounter.get() shouldBe 1
        }
    }
})
