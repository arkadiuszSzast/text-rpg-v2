package com.szastarek.text.rpg.world.command

import com.szastarek.text.rpg.acl.worldCreatorAuthenticatedAccountContext
import com.szastarek.text.rpg.world.draft.command.WorldDraftCreationRequestCommand
import io.kotest.assertions.arrow.core.shouldBeLeft
import io.kotest.assertions.arrow.core.shouldBeRight
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.property.azstring
import kotlin.random.Random

class CreateWorldDraftCommandTest : StringSpec({

	"should create command" {
		// arrange && act && assert
		WorldDraftCreationRequestCommand("name", worldCreatorAuthenticatedAccountContext).shouldBeRight()
	}

	"should accumulate errors" {
		// arrange
		val expectedMessages = listOf("validation.world_name_too_short")

		// act
		val result = WorldDraftCreationRequestCommand(Random.azstring(2), worldCreatorAuthenticatedAccountContext)

		// assert
		result.shouldBeLeft()
		result.value.map { it.message } shouldBe expectedMessages
	}
})
