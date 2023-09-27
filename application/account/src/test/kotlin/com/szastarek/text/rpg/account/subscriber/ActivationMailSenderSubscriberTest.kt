package com.szastarek.text.rpg.account.subscriber

import com.szastarek.text.rpg.account.config.MailTemplatesProperties
import com.szastarek.text.rpg.account.event.AccountEvent
import com.szastarek.text.rpg.account.support.IntegrationTest
import com.szastarek.text.rpg.account.support.anAccountCreatedEvent
import com.szastarek.text.rpg.event.store.EventStoreWriteClient
import com.szastarek.text.rpg.event.store.appendToStream
import com.szastarek.text.rpg.mail.RecordingMailSender
import io.kotest.matchers.booleans.shouldBeTrue
import org.awaitility.kotlin.await
import org.koin.test.inject
import java.time.Duration

class ActivationMailSenderSubscriberTest : IntegrationTest() {

  private val eventStoreWriteClient by inject<EventStoreWriteClient>()
  private val recordingMailSender by inject<RecordingMailSender>()
  private val mailTemplatesProperties by inject<MailTemplatesProperties>()

  init {

    describe("ActivationMailSenderSubscriberTest") {

      it("should send mail on account created event") {
        //arrange
        val event = anAccountCreatedEvent()

        //act
        eventStoreWriteClient.appendToStream<AccountEvent>(event)

        //assert
        await.atMost(Duration.ofMillis(500)).untilAsserted {
          recordingMailSender.hasBeenSent {
            it.to == event.emailAddress && it.subject == mailTemplatesProperties.activateAccount.subject
          }.shouldBeTrue()
        }
      }
    }
  }
}
