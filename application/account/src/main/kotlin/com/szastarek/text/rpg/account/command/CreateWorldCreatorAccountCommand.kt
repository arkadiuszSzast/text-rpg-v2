package com.szastarek.text.rpg.account.command

import arrow.core.Either
import arrow.core.Nel
import arrow.core.nel
import arrow.core.raise.either
import arrow.core.raise.ensure
import arrow.core.raise.zipOrAccumulate
import com.auth0.jwt.JWT
import com.szastarek.text.rpg.account.Account
import com.szastarek.text.rpg.security.JwtToken
import com.szastarek.text.rpg.shared.email.EmailAddress
import com.szastarek.text.rpg.shared.password.HashedPassword
import com.szastarek.text.rpg.shared.password.RawPassword
import com.szastarek.text.rpg.shared.validate.ValidationError
import com.szastarek.text.rpg.shared.validate.ValidationErrors
import com.trendyol.kediatr.CommandWithResult
import kotlinx.datetime.TimeZone
import org.litote.kmongo.Id

typealias CreateWorldCreatorAccountCommandResult = Either<Nel<CreateWorldCreatorAccountError>, CreateWorldCreatorAccountCommandSuccessResult>

data class CreateWorldCreatorAccountCommand(
	val email: EmailAddress,
	val password: HashedPassword,
	val timeZoneId: TimeZone,
	val token: JwtToken,
) : CommandWithResult<CreateWorldCreatorAccountCommandResult> {
	companion object {
		operator fun invoke(
			email: String,
			password: String,
			timeZoneId: String,
			token: String,
		) = either<ValidationErrors, CreateWorldCreatorAccountCommand> {
			zipOrAccumulate(
				{ e1, e2 -> e1 + e2 },
				{ EmailAddress(email).bind() },
				{ RawPassword(password).bind() },
				{
					ensure(runCatching { JWT.decode(token) }.isSuccess) {
						ValidationError(".token", "validation.invalid_invite_world_creator_token").nel()
					}
				},
				{
					ensure(TimeZone.availableZoneIds.contains(timeZoneId)) {
						ValidationError(".timeZoneId", "validation.invalid_timezone").nel()
					}
				},
				{ e, p, _, _ -> CreateWorldCreatorAccountCommand(e, p.hashpw(), TimeZone.of(timeZoneId), JwtToken(token)) },
			)
		}
	}
}

data class CreateWorldCreatorAccountCommandSuccessResult(val accountId: Id<Account>)

enum class CreateWorldCreatorAccountError {
	EmailAlreadyTaken,
	InvalidToken,
}
