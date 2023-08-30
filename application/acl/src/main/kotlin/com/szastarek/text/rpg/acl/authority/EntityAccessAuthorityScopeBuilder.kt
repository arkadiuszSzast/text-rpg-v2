package com.szastarek.text.rpg.acl.authority

import com.szastarek.text.rpg.acl.AclResource
import com.szastarek.text.rpg.acl.AclResourceIdentifier

class EntityAccessAuthorityScopeBuilder<T : AclResource>(private val aclResourceIdentifier: AclResourceIdentifier) {
    private var scopes: MutableList<AuthorityScope<T>> = mutableListOf()

    fun viewScope(vararg predicates: AclResourcePredicate<T>) {
        scope(AuthorityLevel.View, predicates.toList())
    }

    fun createScope(vararg predicates: AclResourcePredicate<T>) {
        scope(AuthorityLevel.Create, predicates.toList())
    }

    fun updateScope(vararg predicates: AclResourcePredicate<T>) {
        scope(AuthorityLevel.Update, predicates.toList())
    }

    fun deleteScope(vararg predicates: AclResourcePredicate<T>) {
        scope(AuthorityLevel.Delete, predicates.toList())
    }

    fun manageScope(vararg predicates: AclResourcePredicate<T>) {
        scope(AuthorityLevel.Manage, predicates.toList())
    }


    fun scope(level: AuthorityLevel, predicates: List<AclResourcePredicate<T>>) {
        scopes.add(AuthorityScope(level, predicates))
    }

    fun build() = EntityAccessAuthority(aclResourceIdentifier, scopes)
}
