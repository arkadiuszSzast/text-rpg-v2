package com.szastarek.text.rpg.account.command

import arrow.core.Either
import arrow.core.Nel
import arrow.core.nel
import arrow.core.raise.either
import arrow.core.raise.ensure
import arrow.core.raise.zipOrAccumulate
import com.szastarek.text.rpg.account.Account
import com.szastarek.text.rpg.shared.email.EmailAddress
import com.szastarek.text.rpg.shared.password.HashedPassword
import com.szastarek.text.rpg.shared.password.RawPassword
import com.szastarek.text.rpg.shared.validate.ValidationError
import com.szastarek.text.rpg.shared.validate.ValidationErrors
import com.trendyol.kediatr.CommandWithResult
import kotlinx.datetime.TimeZone
import org.litote.kmongo.Id

typealias CreateRegularAccountCommandResult = Either<Nel<CreateAccountError>, CreateRegularAccountCommandSuccessResult>

data class CreateRegularAccountCommand(
    val email: EmailAddress,
    val password: HashedPassword,
    val timeZoneId: TimeZone
) : CommandWithResult<CreateRegularAccountCommandResult> {

    companion object {
        operator fun invoke(email: String, password: String, timeZoneId: String) =
            either<ValidationErrors, CreateRegularAccountCommand> {
                zipOrAccumulate(
                    { e1, e2 -> e1 + e2 },
                    { EmailAddress(email).bind() },
                    { RawPassword(password).bind() },
                    {
                        ensure(TimeZone.availableZoneIds.contains(timeZoneId))
                        { ValidationError(".timeZoneId", "validation.invalid_timezone").nel() }
                    },
                    { e, p, _ -> CreateRegularAccountCommand(e, p.hashpw(), TimeZone.of(timeZoneId)) }
                )
            }
    }
}

data class CreateRegularAccountCommandSuccessResult(val accountId: Id<Account>)

enum class CreateAccountError {
    EmailAlreadyTaken
}
