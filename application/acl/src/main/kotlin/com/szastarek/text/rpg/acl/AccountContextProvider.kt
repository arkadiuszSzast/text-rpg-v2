package com.szastarek.text.rpg.acl

interface AccountContextProvider {
    suspend fun currentContext(): AccountContext
}
