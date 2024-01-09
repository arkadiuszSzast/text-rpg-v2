package com.szastarek.text.rpg.world.draft.command.handler

import arrow.core.nel
import com.szastarek.text.rpg.acl.authority.AuthorityCheckException
import com.szastarek.text.rpg.acl.authority.DefaultAuthorizedAccountAbilityProvider
import com.szastarek.text.rpg.acl.regularUserAuthenticatedAccountContext
import com.szastarek.text.rpg.acl.superUserAuthenticatedAccountContext
import com.szastarek.text.rpg.acl.withinAccountContext
import com.szastarek.text.rpg.acl.worldCreatorAuthenticatedAccountContext
import com.szastarek.text.rpg.event.store.InMemoryEventStore
import com.szastarek.text.rpg.security.CoroutineAccountContextProvider
import com.szastarek.text.rpg.world.draft.command.WorldDraftCreationRequestCommand
import com.szastarek.text.rpg.world.draft.command.WorldDraftCreationRequestError
import com.szastarek.text.rpg.world.support.InMemoryWorldDraftListingRepository
import com.szastarek.text.rpg.world.support.aWorldDraftListItem
import com.szastarek.text.rpg.world.support.aWorldName
import io.kotest.assertions.arrow.core.shouldBeLeft
import io.kotest.assertions.arrow.core.shouldBeRight
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec

class WorldDraftCreationRequestCommandHandlerTest : StringSpec({

	val acl = DefaultAuthorizedAccountAbilityProvider(CoroutineAccountContextProvider())
	val repository = InMemoryWorldDraftListingRepository()
	val eventStore = InMemoryEventStore()
	val handler = WorldDraftCreationRequestCommandHandler(acl, eventStore, repository)

	beforeTest {
		eventStore.clear()
		repository.clear()
	}

	"allow initializing world creation for SuperUser" {
		// arrange
		val worldName = aWorldName()
		val command = WorldDraftCreationRequestCommand(worldName, superUserAuthenticatedAccountContext)

		// act
		val result =
			withinAccountContext(superUserAuthenticatedAccountContext) {
				handler.handle(command)
			}

		// assert
		result.shouldBeRight()
	}

	"allow initializing world creation for SuperUser even if already has multiple drafts" {
		// arrange
		repeat(10) { repository.add(aWorldDraftListItem(owner = superUserAuthenticatedAccountContext.accountId)) }

		val worldName = aWorldName()
		val command = WorldDraftCreationRequestCommand(worldName, superUserAuthenticatedAccountContext)

		// act
		val result =
			withinAccountContext(superUserAuthenticatedAccountContext) {
				handler.handle(command)
			}

		// assert
		result.shouldBeRight()
	}

	"allow initializing world creation for WorldCreator" {
		// arrange
		val worldName = aWorldName()
		val command = WorldDraftCreationRequestCommand(worldName, worldCreatorAuthenticatedAccountContext)

		// act
		val result =
			withinAccountContext(worldCreatorAuthenticatedAccountContext) {
				handler.handle(command)
			}

		// assert
	}

	"WorldCreator can have up to 3 drafts" {
		// arrange && act && assert
		repeat(3) {
			val command = WorldDraftCreationRequestCommand(aWorldName(), worldCreatorAuthenticatedAccountContext)

			val result =
				withinAccountContext(worldCreatorAuthenticatedAccountContext) {
					handler.handle(command)
				}

			result.shouldBeRight()

			repository.add(aWorldDraftListItem(owner = worldCreatorAuthenticatedAccountContext.accountId))
		}

		val forthCommand = WorldDraftCreationRequestCommand(aWorldName(), worldCreatorAuthenticatedAccountContext)

		val forthResult =
			withinAccountContext(worldCreatorAuthenticatedAccountContext) {
				handler.handle(forthCommand)
			}
		forthResult.shouldBeLeft(WorldDraftCreationRequestError.MaximumNumberOfDraftsReached.nel())
	}

	"RegularUser cannot create draft" {
		// arrange
		val command = WorldDraftCreationRequestCommand(aWorldName(), regularUserAuthenticatedAccountContext)

		// act && assert
		withinAccountContext(regularUserAuthenticatedAccountContext) {
			shouldThrow<AuthorityCheckException> {
				handler.handle(command)
			}
		}
	}
})
