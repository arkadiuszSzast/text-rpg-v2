package com.szastarek.text.rpg.shared

import kotlinx.serialization.Serializable

sealed interface HttpErrorResponse {
    val type: String
    val title: String
    val instance: String
    val detail: String?
}

@Serializable
data class ProblemHttpErrorResponse(
    override val type: String,
    override val title: String,
    override val instance: String,
    override val detail: String? = null
) : HttpErrorResponse
