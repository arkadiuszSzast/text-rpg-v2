package com.szastarek.text.rpg.world.command

import com.szastarek.text.rpg.acl.AccountId
import com.szastarek.text.rpg.acl.AccountIdProvider
import com.szastarek.text.rpg.world.draft.command.WorldDraftCreationRequestCommand
import io.kotest.assertions.arrow.core.shouldBeLeft
import io.kotest.assertions.arrow.core.shouldBeRight
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.property.azstring
import java.util.UUID
import kotlin.random.Random

class CreateWorldDraftCommandTest : StringSpec({

	"should create command" {
		// arrange
		val accountIdProvider =
			object : AccountIdProvider {
				override val accountId: AccountId = AccountId(UUID.randomUUID().toString())
			}

		// act && assert
		WorldDraftCreationRequestCommand("name", "description", accountIdProvider).shouldBeRight()
	}

	"should accumulate errors" {
		// arrange
		val accountIdProvider =
			object : AccountIdProvider {
				override val accountId: AccountId = AccountId(UUID.randomUUID().toString())
			}
		val expectedMessages =
			listOf(
				"validation.world_name_too_short",
				"validation.world_description_too_short",
			)

		// act
		val result = WorldDraftCreationRequestCommand(Random.azstring(2), Random.azstring(9), accountIdProvider)

		// assert
		result.shouldBeLeft()
		result.value.map { it.message } shouldBe expectedMessages
	}

	"can create with null description" {
		// arrange
		val accountIdProvider =
			object : AccountIdProvider {
				override val accountId: AccountId = AccountId(UUID.randomUUID().toString())
			}

		// act && assert
		WorldDraftCreationRequestCommand("name", null, accountIdProvider).shouldBeRight()
	}
})
