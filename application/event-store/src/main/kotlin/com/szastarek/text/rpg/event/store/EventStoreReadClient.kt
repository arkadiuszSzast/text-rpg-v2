package com.szastarek.text.rpg.event.store

import com.eventstore.dbclient.ReadResult
import com.eventstore.dbclient.ReadStreamOptions

interface EventStoreReadClient {
    suspend fun readStream(streamName: StreamName, options: ReadStreamOptions = ReadStreamOptions.get()): ReadResult
}
