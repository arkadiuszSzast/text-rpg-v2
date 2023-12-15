package com.szastarek.text.rpg.mail.plugin

import com.szastarek.text.rpg.mail.MailSender
import com.szastarek.text.rpg.mail.adapter.sendgrid.SendGridMailSender
import com.szastarek.text.rpg.mail.adapter.sendgrid.SendgridClient
import com.szastarek.text.rpg.mail.config.SendGridProperties
import com.szastarek.text.rpg.shared.config.ConfigKey
import com.szastarek.text.rpg.shared.config.getStringProperty
import io.ktor.client.engine.cio.CIO
import io.ktor.server.application.Application
import org.koin.core.context.loadKoinModules
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

internal val mailModule = module {
    single { SendGridProperties(
        getStringProperty(ConfigKey("sendgrid.api-key")),
        getStringProperty(ConfigKey("sendgrid.host")),
    ) }
    factory { SendgridClient(CIO.create { }, get()) }
    singleOf(::SendGridMailSender) bind MailSender::class
}

internal fun Application.configureKoin() {
    loadKoinModules(mailModule)
}
