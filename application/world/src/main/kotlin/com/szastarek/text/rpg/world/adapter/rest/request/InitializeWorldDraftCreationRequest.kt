package com.szastarek.text.rpg.world.adapter.rest.request

import kotlinx.serialization.Serializable

@Serializable
data class InitializeWorldDraftCreationRequest(val name: String)
