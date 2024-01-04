package com.szastarek.text.rpg.world

import com.szastarek.text.rpg.shared.validate.ValidationError
import io.kotest.assertions.arrow.core.shouldBeLeft
import io.kotest.assertions.arrow.core.shouldBeRight
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.property.azstring
import kotlin.random.Random

class WorldNameTest : StringSpec({

	"should create world name" {
		WorldName("test-world").shouldBeRight()
	}

	"should trim world name" {
		val result = WorldName("  test-world  ").shouldBeRight()
		result.value shouldBe "test-world"
	}

	"should not create world name when it is too short" {
		WorldName("te").shouldBeLeft(ValidationError(".world_name", "validation.world_name_too_short"))
	}

	"should not create world name when it is too long" {
		WorldName(Random.azstring(31)).shouldBeLeft(ValidationError(".world_name", "validation.world_name_too_long"))
	}
})
