package com.szastarek.text.rpg.account.command.handler

import arrow.core.nel
import arrow.core.raise.either
import arrow.core.raise.ensure
import arrow.core.toEitherNel
import com.szastarek.text.rpg.account.AccountAggregateRepository
import com.szastarek.text.rpg.account.AccountStatus
import com.szastarek.text.rpg.account.activation.AccountActivationUrlProvider
import com.szastarek.text.rpg.account.adapter.mail.ActivationAccountMailVariables
import com.szastarek.text.rpg.account.command.ResendActivationMailCommand
import com.szastarek.text.rpg.account.command.ResendActivationMailCommandResult
import com.szastarek.text.rpg.account.command.ResendActivationMailError
import com.szastarek.text.rpg.account.command.ResendActivationMailSuccessResult
import com.szastarek.text.rpg.account.config.ActivateAccountMailProperties
import com.szastarek.text.rpg.acl.authority.AuthorizedAccountAbilityProvider
import com.szastarek.text.rpg.acl.resendActivationLinkFeature
import com.szastarek.text.rpg.mail.Mail
import com.szastarek.text.rpg.mail.MailSender
import com.trendyol.kediatr.CommandWithResultHandler
import org.litote.kmongo.newId

class ResendActivationMailCommandHandler(
  private val accountAggregateRepository: AccountAggregateRepository,
  private val mailSender: MailSender,
  private val mailTemplateProperties: ActivateAccountMailProperties,
  private val accountActivationUrlProvider: AccountActivationUrlProvider,
  private val acl: AuthorizedAccountAbilityProvider

) : CommandWithResultHandler<ResendActivationMailCommand, ResendActivationMailCommandResult> {
  override suspend fun handle(command: ResendActivationMailCommand): ResendActivationMailCommandResult = either {
    acl.ensuring().ensureHasAccessTo(resendActivationLinkFeature)
    val account = accountAggregateRepository.findByEmail(command.email)
      .toEither { ResendActivationMailError.AccountNotFound }
      .toEitherNel().bind()
    ensure(account.status == AccountStatus.Staged) { ResendActivationMailError.InvalidAccountStatus.nel() }

    val activationUrl = accountActivationUrlProvider.provide(account.emailAddress)
    val mailVariables = ActivationAccountMailVariables(activationUrl).toMailVariables()

    val mail = Mail(
      id = newId(),
      subject = mailTemplateProperties.subject,
      from = mailTemplateProperties.sender,
      to = account.emailAddress,
      templateId = mailTemplateProperties.templateId,
      variables = mailVariables
    )

    mailSender.send(mail)

    ResendActivationMailSuccessResult
  }
}
