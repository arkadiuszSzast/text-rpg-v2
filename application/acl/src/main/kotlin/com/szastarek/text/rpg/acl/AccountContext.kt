package com.szastarek.text.rpg.acl

import com.szastarek.text.rpg.acl.authority.AuthoritiesProvider
import com.szastarek.text.rpg.acl.authority.Authority
import com.szastarek.text.rpg.shared.email.EmailAddress
import kotlinx.serialization.Serializable
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

suspend fun AuthenticatedAccountContext.serializable(): SerializableAuthenticatedAccountContext {
	return SerializableAuthenticatedAccountContext(accountId, email, role, getAuthorities())
}

@Serializable
data class SerializableAuthenticatedAccountContext(
	override val accountId: AccountId,
	override val email: EmailAddress,
	override val role: Role,
	val allAuthorities: List<Authority>,
) : AuthenticatedAccountContext {
	override suspend fun getAuthorities(): List<Authority> = allAuthorities
}
