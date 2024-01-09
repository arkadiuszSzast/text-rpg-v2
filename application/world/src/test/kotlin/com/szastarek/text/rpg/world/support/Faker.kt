package com.szastarek.text.rpg.world.support

import com.szastarek.text.rpg.acl.AccountId
import com.szastarek.text.rpg.acl.SerializableAuthenticatedAccountContext
import com.szastarek.text.rpg.acl.getAuthorities
import com.szastarek.text.rpg.acl.superUserAuthenticatedAccountContext
import com.szastarek.text.rpg.shared.validate.getOrThrow
import com.szastarek.text.rpg.world.WorldDescription
import com.szastarek.text.rpg.world.WorldName
import com.szastarek.text.rpg.world.adapter.rest.request.InitializeWorldDraftCreationRequest
import com.szastarek.text.rpg.world.draft.WorldDraft
import com.szastarek.text.rpg.world.draft.WorldDraftListItem
import com.szastarek.text.rpg.world.draft.event.WorldDraftCreationRequestedEvent
import io.github.serpro69.kfaker.faker
import org.litote.kmongo.Id
import org.litote.kmongo.newId
import java.util.UUID

private val faker = faker {}

fun aWorldName(value: String = faker.house.rooms()) = WorldName(value).getOrThrow()

fun aWorldDescription(value: String = faker.random.randomString(10, 5000)) = WorldDescription(value).getOrThrow()

fun aWorldDraftListItem(
	id: Id<WorldDraft> = newId(),
	name: WorldName = aWorldName(),
	description: WorldDescription = aWorldDescription(),
	owner: AccountId = AccountId(UUID.randomUUID().toString()),
) = WorldDraftListItem(
	id,
	name,
	description,
	owner,
)

fun aWorldDraftCreationRequestedEvent(
	id: Id<WorldDraft> = newId(),
	name: WorldName = aWorldName(),
	accountContext: SerializableAuthenticatedAccountContext = superUserSerializableAccountContext(),
) = WorldDraftCreationRequestedEvent(
	id,
	name,
	accountContext,
)

fun superUserSerializableAccountContext() =
	superUserAuthenticatedAccountContext.let {
		SerializableAuthenticatedAccountContext(it.accountId, it.email, it.role, it.role.getAuthorities())
	}

fun anInitializeWorldDraftCreationRequest(name: WorldName = aWorldName()) = InitializeWorldDraftCreationRequest(name.value)
