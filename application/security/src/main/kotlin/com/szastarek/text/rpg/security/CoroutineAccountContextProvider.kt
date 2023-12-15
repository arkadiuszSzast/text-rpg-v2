package com.szastarek.text.rpg.security

import com.szastarek.text.rpg.acl.AccountContext
import com.szastarek.text.rpg.acl.AccountContextProvider
import com.szastarek.text.rpg.acl.AnonymousAccountContext
import com.szastarek.text.rpg.acl.CoroutineAccountContext
import kotlin.coroutines.coroutineContext

class CoroutineAccountContextProvider : AccountContextProvider {
	override suspend fun currentContext(): AccountContext {
		return coroutineContext[CoroutineAccountContext]?.accountContext ?: AnonymousAccountContext
	}
}
