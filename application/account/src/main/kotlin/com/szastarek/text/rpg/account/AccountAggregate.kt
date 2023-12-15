package com.szastarek.text.rpg.account

import arrow.core.Nel
import arrow.core.nel
import arrow.core.raise.either
import arrow.core.raise.ensure
import arrow.core.raise.zipOrAccumulate
import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.szastarek.text.rpg.account.command.ActivateAccountError
import com.szastarek.text.rpg.account.command.ChangePasswordError
import com.szastarek.text.rpg.account.command.LogInAccountError
import com.szastarek.text.rpg.account.command.ResetPasswordError
import com.szastarek.text.rpg.account.event.AccountActivatedEvent
import com.szastarek.text.rpg.account.event.AccountCreatedEvent
import com.szastarek.text.rpg.account.event.AccountPasswordChangedEvent
import com.szastarek.text.rpg.acl.AccountId
import com.szastarek.text.rpg.acl.AclResource
import com.szastarek.text.rpg.acl.AclResourceIdentifier
import com.szastarek.text.rpg.acl.BelongsToAccount
import com.szastarek.text.rpg.acl.Role
import com.szastarek.text.rpg.acl.authority.Authority
import com.szastarek.text.rpg.security.JwtIssuer
import com.szastarek.text.rpg.security.JwtToken
import com.szastarek.text.rpg.shared.Version
import com.szastarek.text.rpg.shared.Versioned
import com.szastarek.text.rpg.shared.email.EmailAddress
import com.szastarek.text.rpg.shared.password.HashedPassword
import com.szastarek.text.rpg.shared.password.RawPassword
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toJavaInstant
import org.litote.kmongo.Id
import org.litote.kmongo.newId
import kotlin.time.Duration

data class AccountAggregate(
	override val id: Id<Account>,
	override val emailAddress: EmailAddress,
	val status: AccountStatus,
	val role: Role,
	val customAuthorities: List<Authority>,
	val password: HashedPassword,
	val createdAt: Instant,
	val timeZone: TimeZone,
	override val version: Version,
) : Account, Versioned, AclResource, BelongsToAccount {
	override val aclResourceIdentifier: AclResourceIdentifier
		get() = AclResourceIdentifier("account-aggregate")
	override val accountId: AccountId
		get() = id.toAccountId()

	companion object {
		fun create(
			emailAddress: EmailAddress,
			role: Role,
			password: HashedPassword,
			timeZone: TimeZone,
			clock: Clock,
		) = AccountCreatedEvent(
			newId(),
			emailAddress,
			AccountStatus.Staged,
			role,
			emptyList(),
			password,
			clock.now(),
			timeZone,
		)
	}

	fun logIn(logInRequestPassword: RawPassword) =
		either<Nel<LogInAccountError>, LoginSuccess> {
			zipOrAccumulate(
				{ ensure(password.matches(logInRequestPassword)) { LogInAccountError.InvalidPassword } },
				{ ensure(status == AccountStatus.Active) { LogInAccountError.AccountNotActive } },
				{ _, _ -> LoginSuccess },
			)
		}

	fun activate() =
		either {
			ensure(status == AccountStatus.Staged) { ActivateAccountError.InvalidAccountStatus.nel() }

			AccountActivatedEvent(id, emailAddress, version.next())
		}

	fun getResetPasswordToken(
		tokenIssuer: JwtIssuer,
		jwtExpiration: Duration,
		clock: Clock,
	): JwtToken {
		return JwtToken(
			JWT.create()
				.withIssuer(tokenIssuer.value)
				.withSubject(emailAddress.value)
				.withExpiresAt(clock.now().plus(jwtExpiration).toJavaInstant())
				.sign(Algorithm.HMAC256(password.value + id.toString())),
		)
	}

	fun resetPassword(
		token: JwtToken,
		issuer: JwtIssuer,
		newPassword: RawPassword,
	) = either {
		val jwtVerifier =
			JWT
				.require(Algorithm.HMAC256(password.value + id.toString()))
				.withSubject(emailAddress.value)
				.withIssuer(issuer.value)
				.build()
		ensure(runCatching { jwtVerifier.verify(token.value) }.isSuccess) { ResetPasswordError.InvalidToken }

		AccountPasswordChangedEvent(id, emailAddress, newPassword.hashpw(), version.next())
	}

	fun changePassword(
		currentPassword: RawPassword,
		newPassword: RawPassword,
	) = either {
		ensure(password.matches(currentPassword)) { ChangePasswordError.InvalidCurrentPassword }

		AccountPasswordChangedEvent(id, emailAddress, newPassword.hashpw(), version.next())
	}
}

object LoginSuccess
