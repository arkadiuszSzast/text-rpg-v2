package com.szastarek.text.rpg.security

import com.szastarek.text.rpg.acl.AccountContext
import com.szastarek.text.rpg.acl.AnonymousAccountContext
import com.szastarek.text.rpg.acl.AuthenticatedAccountContext
import com.szastarek.text.rpg.acl.CoroutineAccountContext
import io.ktor.server.application.ApplicationCall
import kotlin.coroutines.coroutineContext

suspend fun ApplicationCall.getAccountContext(): AccountContext {
	return coroutineContext[CoroutineAccountContext]?.accountContext ?: throw NotAuthenticatedException()
}

suspend fun ApplicationCall.getAuthenticatedAccountContext(): AuthenticatedAccountContext =
	when (val context = getAccountContext()) {
		is AuthenticatedAccountContext -> context
		is AnonymousAccountContext -> throw NotAuthenticatedException()
	}
