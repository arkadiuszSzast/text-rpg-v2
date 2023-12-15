package com.szastarek.text.rpg.account

import com.szastarek.text.rpg.account.command.ActivateAccountError
import com.szastarek.text.rpg.account.command.ResetPasswordError
import com.szastarek.text.rpg.account.event.AccountActivatedEvent
import com.szastarek.text.rpg.account.support.anAccountAggregate
import com.szastarek.text.rpg.security.JwtIssuer
import com.szastarek.text.rpg.shared.aRawPassword
import com.szastarek.text.rpg.utils.FixedClock
import io.kotest.assertions.arrow.core.shouldBeLeft
import io.kotest.assertions.arrow.core.shouldBeRight
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.should
import io.kotest.matchers.shouldBe
import kotlinx.datetime.Clock
import kotlin.time.Duration.Companion.minutes

class AccountAggregateTest : DescribeSpec({

	describe("AccountAggregateTest") {

		it("should activate account") {
			// arrange
			val account = anAccountAggregate(status = AccountStatus.Staged)

			// act
			val result = account.activate()

			// assert
			result.shouldBeRight(AccountActivatedEvent(account.id, account.emailAddress, account.version.next()))
		}

		it("should not activate account when is in suspended status") {
			// arrange
			val account = anAccountAggregate(status = AccountStatus.Suspended)

			// act
			val result = account.activate()

			// assert
			result.shouldBeLeft(listOf(ActivateAccountError.InvalidAccountStatus))
		}

		it("should not activate account when is already active") {
			// arrange
			val account = anAccountAggregate(status = AccountStatus.Active)

			// act
			val result = account.activate()

			// assert
			result.shouldBeLeft(listOf(ActivateAccountError.InvalidAccountStatus))
		}

		it("should reset password") {
			// arrange
			val clock = FixedClock(Clock.System.now())
			val jwtIssuer = JwtIssuer("test-issuer")
			val jwtExpiration = 15.minutes
			val updatedPassword = aRawPassword()
			val account = anAccountAggregate()
			val token = account.getResetPasswordToken(jwtIssuer, jwtExpiration, clock)

			// act
			val event = account.resetPassword(token, jwtIssuer, updatedPassword)

			// assert
			event.shouldBeRight() should {
				it.accountId shouldBe account.id
				it.password.matches(updatedPassword).shouldBeTrue()
				it.version shouldBe account.version.next()
			}
		}

		it("should not update password when token expired") {
			// arrange
			val clock = FixedClock(Clock.System.now().minus(30.minutes))
			val jwtIssuer = JwtIssuer("test-issuer")
			val jwtExpiration = 15.minutes
			val updatedPassword = aRawPassword()
			val account = anAccountAggregate()
			val token = account.getResetPasswordToken(jwtIssuer, jwtExpiration, clock)

			// act
			val event = account.resetPassword(token, jwtIssuer, updatedPassword)

			// assert
			event.shouldBeLeft(ResetPasswordError.InvalidToken)
		}
	}
})
