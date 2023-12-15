package com.szastarek.text.rpg.account.command.handler

import arrow.core.nel
import arrow.core.raise.either
import com.szastarek.text.rpg.account.AccountAggregateRepository
import com.szastarek.text.rpg.account.adapter.mail.ResetPasswordMailVariables
import com.szastarek.text.rpg.account.command.SendResetPasswordCommand
import com.szastarek.text.rpg.account.command.SendResetPasswordCommandResult
import com.szastarek.text.rpg.account.command.SendResetPasswordError
import com.szastarek.text.rpg.account.command.SendResetPasswordSuccessResult
import com.szastarek.text.rpg.account.config.AccountResetPasswordProperties
import com.szastarek.text.rpg.account.config.ResetPasswordMailProperties
import com.szastarek.text.rpg.mail.Mail
import com.szastarek.text.rpg.mail.MailSender
import com.trendyol.kediatr.CommandWithResultHandler
import io.ktor.http.URLBuilder
import kotlinx.datetime.Clock
import org.litote.kmongo.newId

class SendResetPasswordCommandHandler(
	private val accountAggregateRepository: AccountAggregateRepository,
	private val resetPasswordProperties: AccountResetPasswordProperties,
	private val mailProperties: ResetPasswordMailProperties,
	private val mailSender: MailSender,
	private val clock: Clock,
) : CommandWithResultHandler<SendResetPasswordCommand, SendResetPasswordCommandResult> {
	override suspend fun handle(command: SendResetPasswordCommand): SendResetPasswordCommandResult =
		either {
			val email = command.emailAddress
			val (accountResetPasswordUrl, jwtIssuer, jwtExpiration) = resetPasswordProperties
			val account =
				accountAggregateRepository.findByEmail(email)
					.toEither { SendResetPasswordError.AccountNotFound.nel() }.bind()

			val token = account.getResetPasswordToken(jwtIssuer, jwtExpiration, clock)
			val url =
				URLBuilder(accountResetPasswordUrl).apply {
					parameters.append("token", token.value)
				}.build()
			val mailVariables = ResetPasswordMailVariables(url).toMailVariables()

			val mail =
				Mail(
					newId(),
					mailProperties.subject,
					mailProperties.sender,
					account.emailAddress,
					mailProperties.templateId,
					mailVariables,
				)

			mailSender.send(mail)

			SendResetPasswordSuccessResult
		}
}
