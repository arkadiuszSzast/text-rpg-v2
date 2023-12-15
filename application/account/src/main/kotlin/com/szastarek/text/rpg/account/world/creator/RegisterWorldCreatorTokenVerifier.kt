package com.szastarek.text.rpg.account.world.creator

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.auth0.jwt.exceptions.JWTVerificationException
import com.szastarek.text.rpg.account.activation.InvalidJwtResult
import com.szastarek.text.rpg.account.activation.ValidJwtResult
import com.szastarek.text.rpg.account.config.WorldCreatorRegisterProperties
import com.szastarek.text.rpg.security.JwtToken
import com.szastarek.text.rpg.shared.email.EmailAddress
import io.github.oshai.kotlinlogging.KotlinLogging

class RegisterWorldCreatorTokenVerifier(private val worldCreatorRegisterProperties: WorldCreatorRegisterProperties) {
	private val logger = KotlinLogging.logger {}

	fun verify(
		jwtToken: JwtToken,
		subject: EmailAddress,
	): Either<InvalidJwtResult, ValidJwtResult> {
		val jwtConfig = worldCreatorRegisterProperties.jwtConfig
		val verifier =
			JWT.require(Algorithm.HMAC256(jwtConfig.secret.value))
				.withSubject(subject.value)
				.withIssuer(jwtConfig.issuer.value)
				.build()
		return try {
			val decoded = verifier.verify(jwtToken.value)
			ValidJwtResult(decoded).right()
		} catch (ex: JWTVerificationException) {
			logger.error(ex) { "Register world creator token verification failed." }
			InvalidJwtResult.left()
		}
	}
}
