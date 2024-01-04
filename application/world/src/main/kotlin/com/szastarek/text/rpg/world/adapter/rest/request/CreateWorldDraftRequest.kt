package com.szastarek.text.rpg.world.adapter.rest.request

import kotlinx.serialization.Serializable

@Serializable
data class CreateWorldDraftRequest(val name: String, val description: String?)
