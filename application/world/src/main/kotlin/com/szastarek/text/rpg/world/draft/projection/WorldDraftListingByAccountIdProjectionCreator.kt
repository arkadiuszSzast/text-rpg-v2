package com.szastarek.text.rpg.world.draft.projection

import com.szastarek.text.rpg.event.store.EventStoreProjectionsClient
import com.szastarek.text.rpg.event.store.ProjectionName
import com.szastarek.text.rpg.event.store.ProjectionQuery

class WorldDraftListingByAccountIdProjectionCreator(private val projectionsClient: EventStoreProjectionsClient) {
	suspend fun createOrUpdateAndEnable() = projectionsClient.createOrUpdateContinuous(name, query)

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
				    ${"$"}init: function() {
				        return {
							waitingForApproval: [],
				            drafts: []
				        }
				    },
				    'WorldDraft-CreationRequested': function(state, event) {
				        const draft = {
				                draftId: event.body.draftId,
				                name: event.body.name,
				                ownerId: event.body.ownerId
				        };
				        state.waitingForApproval.push(draft);
				    },
						'WorldDraft-CreationApproved': function(state, event) {
							const draftId = event.body.draftId;
							const draft = state.waitingForApproval.find(draft => draft.draftId === draftId);
							if (draft) {
								state.drafts.push(draft);
								state.waitingForApproval.splice(state.waitingForApproval.indexOf(draft), 1);
							}
						},
						'WorldDraft-CreationRejected': function(state, event) {
							const draftId = event.body.draftId;
							const draft = state.waitingForApproval.find(draft => draft.draftId === draftId);
							if (draft) {
								state.waitingForApproval.splice(state.waitingForApproval.indexOf(draft), 1);
							}
						}
				});
				""".trimIndent(),
			)
	}
}

// fromCategory('WorldDraft')
// .partitionBy(function(event) {
// 	return event.body.ownerId
// })
// .when({
// 	${"$"}init: function() {
// 		return {
// 			waitingForApproval: [],
// 			drafts: []
// 		}
// 	},
// 	'WorldDraft-CreationRequested': function(state, event) {
// 		const draft = {
// 				draftId: event.body.draftId,
// 				name: event.body.name,
// 				ownerId: event.body.creatorAccountContext.accountId
// 		};
// 		state.waitingForApproval.push(draft);
// 	},
// 	'WorldDraft-CreationApproved': function(state, event) {
// 		const draftId = event.body.draftId;
// 		const draft = state.waitingForApproval.find(draft => draft.draftId === draftId);
// 		if (draft) {
// 			state.drafts.push(draft);
// 			waitingForApproval.splice(waitingForApproval.indexOf(draft), 1);
// 		}
// 	},
// 	'WorldDraft-CreationRejected': function(state, event) {
// 		const draftId = event.body.draftId;
// 		const draft = state.waitingForApproval.find(draft => draft.draftId === draftId);
// 		if (draft) {
// 			waitingForApproval.splice(waitingForApproval.indexOf(draft), 1);
// 		}
// 	}
// });
