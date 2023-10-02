package com.szastarek.text.rpg.account.command.handler

import arrow.core.nel
import arrow.core.raise.either
import arrow.core.raise.ensure
import com.szastarek.text.rpg.account.AccountAggregateRepository
import com.szastarek.text.rpg.account.adapter.mail.InviteWorldCreatorMailVariables
import com.szastarek.text.rpg.account.command.InviteWorldCreatorCommand
import com.szastarek.text.rpg.account.command.InviteWorldCreatorCommandResult
import com.szastarek.text.rpg.account.command.InviteWorldCreatorCommandSuccessResult
import com.szastarek.text.rpg.account.command.InviteWorldCreatorError
import com.szastarek.text.rpg.account.config.InviteWorldCreatorMailProperties
import com.szastarek.text.rpg.account.world.creator.WorldCreatorRegisterUrlProvider
import com.szastarek.text.rpg.acl.authority.AuthorizedAccountAbilityProvider
import com.szastarek.text.rpg.acl.inviteWorldCreatorFeature
import com.szastarek.text.rpg.mail.Mail
import com.szastarek.text.rpg.mail.MailSender
import com.trendyol.kediatr.CommandWithResultHandler
import org.litote.kmongo.newId

class InviteWorldCreatorCommandHandler(
  private val worldCreatorRegisterUrlProvider: WorldCreatorRegisterUrlProvider,
  private val inviteWorldCreatorMailProperties: InviteWorldCreatorMailProperties,
  private val accountAggregateRepository: AccountAggregateRepository,
  private val mailSender: MailSender,
  private val acl: AuthorizedAccountAbilityProvider
) : CommandWithResultHandler<InviteWorldCreatorCommand, InviteWorldCreatorCommandResult> {
  override suspend fun handle(command: InviteWorldCreatorCommand): InviteWorldCreatorCommandResult = either {
    val email = command.email
    acl.ensuring().ensureHasAccessTo(inviteWorldCreatorFeature)
    ensure(accountAggregateRepository.findByEmail(email).isNone()) { InviteWorldCreatorError.EmailAlreadyTaken.nel() }

    val worldCreatorRegisterUrl = worldCreatorRegisterUrlProvider.provide(email)
    val mailVariables = InviteWorldCreatorMailVariables(worldCreatorRegisterUrl).toMailVariables()
    val mail = Mail(
      newId(),
      inviteWorldCreatorMailProperties.subject,
      inviteWorldCreatorMailProperties.sender,
      email,
      inviteWorldCreatorMailProperties.templateId,
      mailVariables
    )

    mailSender.send(mail)

    InviteWorldCreatorCommandSuccessResult
  }
}