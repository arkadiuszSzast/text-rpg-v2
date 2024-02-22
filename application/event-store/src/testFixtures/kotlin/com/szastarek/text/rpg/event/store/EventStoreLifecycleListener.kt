package com.szastarek.text.rpg.event.store

import io.kotest.core.listeners.TestListener
import io.kotest.core.spec.Spec
import io.kotest.core.test.TestCase
import io.kotest.core.test.TestResult

class EventStoreLifecycleListener(private val container: EventStoreContainer) : TestListener {
	override suspend fun afterEach(
		testCase: TestCase,
		result: TestResult,
	) {
		container.restart()
		super.afterEach(testCase, result)
	}

	override suspend fun afterSpec(spec: Spec) {
		container.stop()
		super.afterSpec(spec)
	}
}
