package com.szastarek.text.rpg.event.store

import io.kotest.core.listeners.TestListener
import io.kotest.core.spec.Spec
import io.kotest.core.test.TestCase

class EventStoreLifecycleListener(private val container: EventStoreContainer): TestListener {
    override suspend fun beforeEach(testCase: TestCase) {
        container.restart()
    }

    override suspend fun afterSpec(spec: Spec) {
        container.stop()
    }

}