package com.szastarek.text.rpg.acl

import com.szastarek.text.rpg.acl.authority.AuthoritiesProvider
import com.szastarek.text.rpg.acl.authority.Authority
import com.szastarek.text.rpg.shared.email.EmailAddress
import kotlin.coroutines.coroutineContext

sealed interface AccountContext : AuthoritiesProvider

interface AuthenticatedAccountContext : AccountContext, AccountIdProvider, HasRole {
	val email: EmailAddress
}

data object AnonymousAccountContext : AccountContext {
	override suspend fun getAuthorities(): List<Authority> {
		return coroutineContext[CoroutineInjectedAuthorityContext]?.authorities ?: emptyList()
	}
}
