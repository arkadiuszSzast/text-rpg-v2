package com.szastarek.text.rpg.shared.validate

data class ValidationException(val validationErrors: List<ValidationError>) :
    RuntimeException(validationErrors.joinToString(","))
