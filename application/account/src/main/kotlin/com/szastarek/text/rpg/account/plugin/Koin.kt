package com.szastarek.text.rpg.account.plugin

import com.szastarek.text.rpg.account.AccountAggregateRepository
import com.szastarek.text.rpg.account.RefreshTokenRepository
import com.szastarek.text.rpg.account.activation.AccountActivationTokenVerifier
import com.szastarek.text.rpg.account.activation.AccountActivationUrlProvider
import com.szastarek.text.rpg.account.adapter.event.store.AccountAggregateEventStoreRepository
import com.szastarek.text.rpg.account.adapter.redis.RefreshTokenRedisRepository
import com.szastarek.text.rpg.account.command.handler.ActivateAccountCommandHandler
import com.szastarek.text.rpg.account.command.handler.ChangePasswordCommandHandler
import com.szastarek.text.rpg.account.command.handler.CreateRegularAccountCommandHandler
import com.szastarek.text.rpg.account.command.handler.CreateWorldCreatorAccountCommandHandler
import com.szastarek.text.rpg.account.command.handler.InviteWorldCreatorCommandHandler
import com.szastarek.text.rpg.account.command.handler.LogInAccountCommandHandler
import com.szastarek.text.rpg.account.command.handler.RefreshAuthTokenCommandHandler
import com.szastarek.text.rpg.account.command.handler.ResendActivationMailCommandHandler
import com.szastarek.text.rpg.account.command.handler.ResetPasswordCommandHandler
import com.szastarek.text.rpg.account.command.handler.SendResetPasswordCommandHandler
import com.szastarek.text.rpg.account.config.AccountActivationProperties
import com.szastarek.text.rpg.account.config.AccountResetPasswordProperties
import com.szastarek.text.rpg.account.config.ActivateAccountMailProperties
import com.szastarek.text.rpg.account.config.InviteWorldCreatorMailProperties
import com.szastarek.text.rpg.account.config.ResetPasswordMailProperties
import com.szastarek.text.rpg.account.config.WorldCreatorRegisterProperties
import com.szastarek.text.rpg.account.subscriber.ActivationMailSenderSubscriber
import com.szastarek.text.rpg.account.world.creator.RegisterWorldCreatorTokenVerifier
import com.szastarek.text.rpg.account.world.creator.WorldCreatorRegisterUrlProvider
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
import io.ktor.http.Url
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

internal val accountConfigModule =
	module {
		single {
			ActivateAccountMailProperties(
				MailTemplateId(getStringProperty(ConfigKey("mail.activateAccount.templateId"))),
				EmailAddress(getStringProperty(ConfigKey("mail.activateAccount.sender"))).getOrThrow(),
				MailSubject(getStringProperty(ConfigKey("mail.activateAccount.subject"))),
			)
		}
		single {
			ResetPasswordMailProperties(
				MailTemplateId(getStringProperty(ConfigKey("mail.resetPassword.templateId"))),
				EmailAddress(getStringProperty(ConfigKey("mail.resetPassword.sender"))).getOrThrow(),
				MailSubject(getStringProperty(ConfigKey("mail.resetPassword.subject"))),
			)
		}
		single {
			InviteWorldCreatorMailProperties(
				MailTemplateId(getStringProperty(ConfigKey("mail.inviteWorldCreator.templateId"))),
				EmailAddress(getStringProperty(ConfigKey("mail.inviteWorldCreator.sender"))).getOrThrow(),
				MailSubject(getStringProperty(ConfigKey("mail.inviteWorldCreator.subject"))),
			)
		}
		single {
			AccountActivationProperties(
				Url(getStringProperty(ConfigKey("activateAccount.url"))),
				JwtProperties(
					JwtSecret(getStringProperty(ConfigKey("activateAccount.jwt.secret"))),
					JwtIssuer(getStringProperty(ConfigKey("activateAccount.jwt.issuer"))),
					getLongProperty(ConfigKey("activateAccount.jwt.expirationInMillis")).milliseconds,
				),
			)
		}
		single {
			AccountResetPasswordProperties(
				Url(getStringProperty(ConfigKey("resetPassword.url"))),
				JwtIssuer(getStringProperty(ConfigKey("resetPassword.jwt.issuer"))),
				getLongProperty(ConfigKey("resetPassword.jwt.expirationInMillis")).milliseconds,
			)
		}
		single {
			WorldCreatorRegisterProperties(
				Url(getStringProperty(ConfigKey("worldCreatorRegister.url"))),
				JwtProperties(
					JwtSecret(getStringProperty(ConfigKey("worldCreatorRegister.jwt.secret"))),
					JwtIssuer(getStringProperty(ConfigKey("worldCreatorRegister.jwt.issuer"))),
					getLongProperty(ConfigKey("worldCreatorRegister.jwt.expirationInMillis")).milliseconds,
				),
			)
		}
	}

internal val accountModule =
	module {
		singleOf(::LogInAccountCommandHandler)
		singleOf(::CreateRegularAccountCommandHandler)
		singleOf(::AccountAggregateEventStoreRepository) bind AccountAggregateRepository::class
		singleOf(::ActivationMailSenderSubscriber) { createdAtStart() }
		singleOf(::AccountActivationUrlProvider)
		singleOf(::ActivateAccountCommandHandler)
		singleOf(::AccountActivationTokenVerifier)
		singleOf(::SendResetPasswordCommandHandler)
		singleOf(::ResetPasswordCommandHandler)
		singleOf(::ChangePasswordCommandHandler)
		singleOf(::WorldCreatorRegisterUrlProvider)
		singleOf(::InviteWorldCreatorCommandHandler)
		singleOf(::CreateWorldCreatorAccountCommandHandler)
		singleOf(::RegisterWorldCreatorTokenVerifier)
		singleOf(::ResendActivationMailCommandHandler)
		singleOf(::RefreshTokenRedisRepository) bind RefreshTokenRepository::class
		singleOf(::RefreshAuthTokenCommandHandler)
	}

internal fun Application.configureKoin() {
	if (GlobalContext.getOrNull() == null) {
		install(Koin) {
			// TODO remove when Koin 3.5.2 would be released
			GlobalContext.startKoin(this)
		}
	}
	loadKoinModules(listOf(accountModule, accountConfigModule))
}
