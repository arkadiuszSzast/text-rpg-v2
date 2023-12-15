package com.szastarek.text.rpg.account.activation

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.auth0.jwt.exceptions.JWTVerificationException
import com.auth0.jwt.interfaces.DecodedJWT
import com.szastarek.text.rpg.account.config.AccountActivationProperties
import com.szastarek.text.rpg.security.JwtToken
import io.github.oshai.kotlinlogging.KotlinLogging

class AccountActivationTokenVerifier(private val accountActivationProperties: AccountActivationProperties) {
	private val logger = KotlinLogging.logger {}

	fun verify(jwtToken: JwtToken): Either<InvalidJwtResult, ValidJwtResult> {
		val jwtConfig = accountActivationProperties.jwtConfig
		val verifier =
			JWT.require(Algorithm.HMAC256(jwtConfig.secret.value))
				.withIssuer(jwtConfig.issuer.value)
				.build()
		return try {
			val decoded = verifier.verify(jwtToken.value)
			ValidJwtResult(decoded).right()
		} catch (ex: JWTVerificationException) {
			logger.error(ex) { "Account activation token verification failed." }
			InvalidJwtResult.left()
		}
	}
}

object InvalidJwtResult

data class ValidJwtResult(val decodedJWT: DecodedJWT)
