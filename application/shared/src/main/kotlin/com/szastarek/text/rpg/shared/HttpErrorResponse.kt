package com.szastarek.text.rpg.shared

import com.szastarek.text.rpg.shared.validate.ValidationError
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

@Serializable
data class ValidationErrorHttpMessage(
    val validationErrors: List<ValidationError>,
    override val type: String,
    override val instance: String
) : HttpErrorResponse {
    override val title: String = "Your request parameters did not validate."
    override val detail: String? = null
}
