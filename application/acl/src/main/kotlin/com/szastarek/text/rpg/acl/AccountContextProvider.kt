package com.szastarek.text.rpg.acl

fun interface AccountContextProvider {
	suspend fun currentContext(): AccountContext
}
