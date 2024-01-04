package com.szastarek.text.rpg.world.draft.projection

import com.szastarek.text.rpg.event.store.EventStoreProjectionsClient
import com.szastarek.text.rpg.event.store.ProjectionName
import com.szastarek.text.rpg.event.store.ProjectionQuery
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking

class WorldDraftListingByAccountIdProjection(private val projectionsClient: EventStoreProjectionsClient) {
	init {
		createOrUpdateAndEnable()
	}

	private fun createOrUpdateAndEnable() =
		runBlocking(Dispatchers.IO) {
			projectionsClient.createOrUpdateContinuous(name, query)
		}

	companion object {
		val name = ProjectionName("drafts-list-by-account-id")
		val query =
			ProjectionQuery(
				"""
				fromCategory('WorldDraft')
				.partitionBy(function(event) {
				    return event.body.ownerId
				})
				.when({
				    ${"$"}init: function(){
				        return {
				            drafts: []
				        }
				    },
				    'WorldDraft-Created': function(state, event){
				        const draft = {
				                draftId: event.body.draftId,
				                name: event.body.name,
				                description: event.description,
				                ownerId: event.body.ownerId
				        };
				        state.drafts.push(draft);
				    }
				});
				""".trimIndent(),
			)
	}
}
