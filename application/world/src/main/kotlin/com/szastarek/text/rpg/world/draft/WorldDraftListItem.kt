package com.szastarek.text.rpg.world.draft

import com.szastarek.text.rpg.acl.AccountId
import com.szastarek.text.rpg.world.WorldDescription
import com.szastarek.text.rpg.world.WorldName
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import org.litote.kmongo.Id

@Serializable
data class WorldDraftListing(
	val drafts: List<WorldDraftListItem>,
)

@Serializable
data class WorldDraftListItem(
	@Contextual override val draftId: Id<WorldDraft>,
	val name: WorldName,
	val description: WorldDescription?,
	val ownerId: AccountId,
) : WorldDraft
