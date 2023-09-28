package com.szastarek.text.rpg.account.plugin

import com.szastarek.text.rpg.account.AccountAggregateRepository
import com.szastarek.text.rpg.account.activation.AccountActivationUrlProvider
import com.szastarek.text.rpg.account.adapter.event.store.AccountAggregateEventStoreRepository
import com.szastarek.text.rpg.account.command.handler.LogInAccountCommandHandler
import com.szastarek.text.rpg.account.command.handler.CreateRegularAccountCommandHandler
import com.szastarek.text.rpg.account.config.AccountActivationProperties
import com.szastarek.text.rpg.account.config.MailTemplate
import com.szastarek.text.rpg.account.config.MailTemplatesProperties
import com.szastarek.text.rpg.account.subscriber.ActivationMailSenderSubscriber
import com.szastarek.text.rpg.mail.MailSubject
import com.szastarek.text.rpg.mail.MailTemplateId
import com.szastarek.text.rpg.security.JwtIssuer
import com.szastarek.text.rpg.security.JwtProperties
import com.szastarek.text.rpg.security.JwtSecret
import com.szastarek.text.rpg.shared.config.ConfigKey
import com.szastarek.text.rpg.shared.config.getLongProperty
import com.szastarek.text.rpg.shared.config.getStringProperty
import com.szastarek.text.rpg.shared.email.EmailAddress
import com.szastarek.text.rpg.shared.validate.getOrThrow
import io.ktor.http.*
import io.ktor.server.application.Application
import io.ktor.server.application.install
import org.koin.core.context.GlobalContext
import org.koin.core.context.loadKoinModules
import org.koin.core.module.dsl.createdAtStart
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module
import org.koin.ktor.plugin.Koin
import kotlin.time.Duration.Companion.milliseconds

internal val accountConfigModule = module {
  single {
    MailTemplatesProperties(
      MailTemplate(
        MailTemplateId(getStringProperty(ConfigKey("mail.activateAccount.templateId"))),
        EmailAddress(getStringProperty(ConfigKey("mail.activateAccount.sender"))).getOrThrow(),
        MailSubject(getStringProperty(ConfigKey("mail.activateAccount.subject")))
      )
    )
  }
  single {
    AccountActivationProperties(
      Url(getStringProperty(ConfigKey("activateAccount.url"))),
      JwtProperties(
        JwtSecret(getStringProperty(ConfigKey("activateAccount.jwt.secret"))),
        JwtIssuer(getStringProperty(ConfigKey("activateAccount.jwt.issuer"))),
        getLongProperty(ConfigKey("activateAccount.jwt.expirationInMillis")).milliseconds
      )
    )
  }
}

internal val accountModule = module {
  singleOf(::LogInAccountCommandHandler)
  singleOf(::CreateRegularAccountCommandHandler)
  singleOf(::AccountAggregateEventStoreRepository) bind AccountAggregateRepository::class
  singleOf(::ActivationMailSenderSubscriber) { createdAtStart() }
  singleOf(::AccountActivationUrlProvider)
}

internal fun Application.configureKoin() {
  if (GlobalContext.getOrNull() == null) {
    install(Koin)
  }
  loadKoinModules(listOf(accountModule, accountConfigModule))
}
