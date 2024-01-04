package com.szastarek.text.rpg.world.draft.command

import arrow.core.Either
import arrow.core.Nel
import arrow.core.raise.either
import arrow.core.raise.zipOrAccumulate
import com.szastarek.text.rpg.acl.AccountIdProvider
import com.szastarek.text.rpg.shared.validate.ValidationErrors
import com.szastarek.text.rpg.world.WorldDescription
import com.szastarek.text.rpg.world.WorldName
import com.szastarek.text.rpg.world.draft.WorldDraft
import com.trendyol.kediatr.CommandWithResult
import org.litote.kmongo.Id

typealias WorldDraftCreationRequestCommandResult = Either<Nel<WorldDraftCreationRequestError>, WorldDraftCreationRequestCommandSuccessResult>

data class WorldDraftCreationRequestCommand(
	val name: WorldName,
	val description: WorldDescription?,
	val accountIdProvider: AccountIdProvider,
) : CommandWithResult<WorldDraftCreationRequestCommandResult> {
	companion object {
		operator fun invoke(
			name: String,
			description: String?,
			accountIdProvider: AccountIdProvider,
		) = either<ValidationErrors, WorldDraftCreationRequestCommand> {
			zipOrAccumulate(
				{ e1, e2 -> e1 + e2 },
				{ WorldName(name).bind() },
				{ description?.let { WorldDescription(it).bind() } },
				{ worldName, worldDescription ->
					WorldDraftCreationRequestCommand(
						worldName,
						worldDescription,
						accountIdProvider,
					)
				},
			)
		}
	}
}

data class WorldDraftCreationRequestCommandSuccessResult(val draftId: Id<WorldDraft>)

enum class WorldDraftCreationRequestError {
	MaximumNumberOfDraftsReached,
}
