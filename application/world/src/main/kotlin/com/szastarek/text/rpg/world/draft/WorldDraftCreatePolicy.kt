package com.szastarek.text.rpg.world.draft

import arrow.core.Either
import arrow.core.Nel
import arrow.core.flattenOrAccumulate
import arrow.core.nel
import arrow.core.raise.either
import arrow.core.raise.ensure
import com.szastarek.text.rpg.acl.AuthenticatedAccountContext
import com.szastarek.text.rpg.acl.Roles
import com.szastarek.text.rpg.world.draft.command.WorldDraftCreationRequestError

fun interface WorldDraftCreatePolicy {
	companion object {
		fun get() =
			listOf(
				worldCreatorMaximumNumberOfExistingDraftsPolicy,
				onlySuperUserAndWorldCreatorsCanCreateWorldDraftPolicy,
			)
	}

	fun apply(
		creator: AuthenticatedAccountContext,
		otherDraftsBelongingToCreator: List<WorldDraftListItem>,
	): WorldDraftCreatePolicyResult
}

val worldCreatorMaximumNumberOfExistingDraftsPolicy =
	WorldDraftCreatePolicy { creator, otherDraftsBelongingToCreator ->
		either {
			ensure((creator.role == Roles.WorldCreator.role && otherDraftsBelongingToCreator.size >= 3).not()) {
				WorldDraftCreationRequestError.MaximumNumberOfDraftsReached.nel()
			}
			Allowance
		}
	}

val onlySuperUserAndWorldCreatorsCanCreateWorldDraftPolicy =
	WorldDraftCreatePolicy { creator, _ ->
		either {
			ensure(creator.role == Roles.SuperUser.role || creator.role == Roles.WorldCreator.role) {
				WorldDraftCreationRequestError.AccountNotAllowedToCreateDraft.nel()
			}
			Allowance
		}
	}

typealias WorldDraftCreatePolicyResult = Either<Nel<WorldDraftCreationRequestError>, Allowance>

data object Allowance

fun List<WorldDraftCreatePolicy>.isAllowed(
	creator: AuthenticatedAccountContext,
	otherDraftsBelongingToCreator: List<WorldDraftListItem>,
): WorldDraftCreatePolicyResult {
	return map { it.apply(creator, otherDraftsBelongingToCreator) }
		.flattenOrAccumulate()
		.map { Allowance }
}
