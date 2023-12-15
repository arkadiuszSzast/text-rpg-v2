package com.szastarek.text.rpg.acl.authority

import com.szastarek.text.rpg.acl.AccountContext
import com.szastarek.text.rpg.acl.AclResource
import com.szastarek.text.rpg.acl.AnonymousAccountContext
import com.szastarek.text.rpg.acl.AuthenticatedAccountContext
import com.szastarek.text.rpg.acl.BelongsToAccount
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AuthorityScope<T : AclResource>(
	val level: AuthorityLevel,
	val predicates: List<AclResourcePredicate<T>> = emptyList(),
)

@Serializable
sealed interface AclResourcePredicate<in T : AclResource> {
	fun isSatisfiedBy(
		resource: T,
		accountContext: AccountContext,
	): Boolean
}

@Serializable
@SerialName("AclResourceBelongsToAccountPredicate")
class AclResourceBelongsToAccountPredicate<T> : AclResourcePredicate<T> where T : AclResource, T : BelongsToAccount {
	override fun isSatisfiedBy(
		resource: T,
		accountContext: AccountContext,
	): Boolean {
		return when (accountContext) {
			is AnonymousAccountContext -> false
			is AuthenticatedAccountContext -> resource.accountId == accountContext.accountId
		}
	}

	override fun equals(other: Any?): Boolean {
		if (this === other) return true
		if (javaClass != other?.javaClass) return false
		return true
	}

	override fun hashCode(): Int {
		return javaClass.hashCode()
	}
}
