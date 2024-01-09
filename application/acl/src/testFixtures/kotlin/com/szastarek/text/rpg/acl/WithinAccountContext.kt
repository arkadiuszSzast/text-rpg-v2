package com.szastarek.text.rpg.acl

import kotlinx.coroutines.withContext
import kotlin.coroutines.coroutineContext

suspend fun <T> withinAccountContext(
	accountContext: AccountContext,
	block: suspend () -> T,
) = withContext(coroutineContext + CoroutineAccountContext(accountContext)) {
	block()
}
