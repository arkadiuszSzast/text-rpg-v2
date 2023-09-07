package com.szastarek.text.rpg.event.store

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import java.util.UUID

class EventMetadataTest : DescribeSpec({

    describe("EventMetadataTest") {

        it("should generate metadata based on given causing metadata") {
            //arrange
            val causingMetadata = EventMetadataBuilder(
                AggregateId(UUID.randomUUID().toString()),
                EventCategory("account"),
                EventType("account-created")
            ).build()

            //act
            val result = EventMetadataBuilder(
                AggregateId(UUID.randomUUID().toString()),
                EventCategory("email"),
                EventType("email-sent")
            ).causedBy(causingMetadata).build()

            //assert
            result.correlationId shouldBe causingMetadata.correlationId
            result.causationId shouldBe CausationId(causingMetadata.eventId.value)
        }
    }
})
