package com.szastarek.text.rpg.event.store

interface DomainEvent {
    fun getMetadata(causedBy: EventMetadata? = null): EventMetadata
}
