package com.szastarek.text.rpg.event.store

import com.eventstore.dbclient.ExpectedRevision

data class InvalidExpectedRevisionException(
	val streamName: String,
	val nextExpectedRevision: ExpectedRevision,
	val actualRevision: ExpectedRevision,
) : RuntimeException(
		"Exception when appending to stream $streamName. " +
			"Expected revision $nextExpectedRevision is different than actual revision $actualRevision",
	)
