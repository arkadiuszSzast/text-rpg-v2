package com.szastarek.text.rpg.world.draft.command

import arrow.core.Either
import arrow.core.Nel
import arrow.core.raise.either
import com.szastarek.text.rpg.acl.AuthenticatedAccountContext
import com.szastarek.text.rpg.world.WorldName
import com.szastarek.text.rpg.world.draft.WorldDraft
import com.trendyol.kediatr.CommandWithResult
import org.litote.kmongo.Id

typealias WorldDraftCreationRequestCommandResult = Either<Nel<WorldDraftCreationRequestError>, WorldDraftCreationRequestCommandSuccessResult>

data class WorldDraftCreationRequestCommand(
	val name: WorldName,
	val authenticatedAccountContext: AuthenticatedAccountContext,
) : CommandWithResult<WorldDraftCreationRequestCommandResult> {
	companion object {
		operator fun invoke(
			name: String,
			authenticatedAccountContext: AuthenticatedAccountContext,
		) = either {
			val worldName = WorldName(name).bind()
			WorldDraftCreationRequestCommand(worldName, authenticatedAccountContext)
		}
	}
}

data class WorldDraftCreationRequestCommandSuccessResult(val draftId: Id<WorldDraft>)

enum class WorldDraftCreationRequestError {
	AccountNotAllowedToCreateDraft,
	MaximumNumberOfDraftsReached,
}
