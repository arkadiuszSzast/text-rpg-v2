package com.szastarek.text.rpg.world

import arrow.core.nel
import com.szastarek.text.rpg.shared.validate.ValidationError
import io.kotest.assertions.arrow.core.shouldBeLeft
import io.kotest.assertions.arrow.core.shouldBeRight
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.property.azstring
import kotlin.random.Random

class WorldDescriptionTest : StringSpec({

	"should create world description" {
		WorldDescription("test-world").shouldBeRight()
	}

	"should trim world description" {
		val result = WorldDescription("  test-world  ").shouldBeRight()
		result.value shouldBe "test-world"
	}

	"should not create world description when it is too short" {
		WorldDescription(Random.azstring(9))
			.shouldBeLeft(
				ValidationError(".world_description", "validation.world_description_too_short").nel(),
			)
	}

	"should not create world description when it is too long" {
		WorldDescription(Random.azstring(5001))
			.shouldBeLeft(
				ValidationError(".world_description", "validation.world_description_too_long").nel(),
			)
	}
})
