package com.szastarek.text.rpg.account.command

import arrow.core.Either
import arrow.core.Nel
import arrow.core.raise.either
import arrow.core.raise.ensure
import arrow.core.raise.zipOrAccumulate
import com.szastarek.text.rpg.account.Account
import com.szastarek.text.rpg.shared.email.EmailAddress
import com.szastarek.text.rpg.shared.password.RawPassword
import com.szastarek.text.rpg.shared.validate.ValidationError
import com.szastarek.text.rpg.shared.validate.ValidationErrors
import com.trendyol.kediatr.CommandWithResult
import com.trendyol.kediatr.CommandWithResultHandler
import kotlinx.datetime.TimeZone
import org.litote.kmongo.Id
import org.litote.kmongo.newId

data class CreateAccountCommand(val email: EmailAddress, val password: RawPassword, val timeZoneId: TimeZone) :
    CommandWithResult<Either<Nel<CreateAccountError>, CreateAccountCommandResult>> {

    companion object {
        operator fun invoke(email: String, password: String, timeZoneId: String) =
            either<ValidationErrors, CreateAccountCommand> {
                zipOrAccumulate(
                    { EmailAddress(email).bind() },
                    { RawPassword(password).bind() },
                    {
                        ensure(TimeZone.availableZoneIds.contains(timeZoneId))
                        { ValidationError(".timeZoneId", "validation.invalid_timezone") }
                    },
                    { e, p, _ -> CreateAccountCommand(e, p, TimeZone.of(timeZoneId)) }
                )
            }
    }
}

data class CreateAccountCommandResult(val accountId: Id<Account>)

enum class CreateAccountError {
    EmailAlreadyTaken
}

class CreateAccountCommandHandler() : CommandWithResultHandler<CreateAccountCommand, Either<Nel<CreateAccountError>, CreateAccountCommandResult>> {
    override suspend fun handle(command: CreateAccountCommand): Either<Nel<CreateAccountError>, CreateAccountCommandResult> {
        println(command)

        return Either.Right(CreateAccountCommandResult(newId()))
    }
}
