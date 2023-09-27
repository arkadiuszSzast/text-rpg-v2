package com.szastarek.text.rpg.event.store

import com.eventstore.dbclient.ExpectedRevision
import com.eventstore.dbclient.Position

data class EventStoreWriteResult(val logPosition: Position, val nextExpectedRevision: ExpectedRevision)