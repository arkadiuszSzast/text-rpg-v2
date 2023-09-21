package com.szastarek.text.rpg.shared.validate

import arrow.core.Nel
import kotlinx.serialization.Serializable

@Serializable
data class ValidationError(val dataPath: String, val message: String)

typealias ValidationErrors = Nel<ValidationError>
