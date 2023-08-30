package com.szastarek.text.rpg.acl

import com.szastarek.text.rpg.acl.authority.AuthorityCheckException
import com.szastarek.text.rpg.acl.authority.AuthorizedAccountAbilityProvider
import com.szastarek.text.rpg.acl.authority.DefaultAuthorizedAccountAbilityEnsureProvider
import com.szastarek.text.rpg.acl.authority.Deny

class DenyAllAuthorizedAccountAbilityProvider : AuthorizedAccountAbilityProvider {
    override suspend fun hasAccessTo(feature: Feature) = Deny(AuthorityCheckException("Deny all"))

    override suspend fun <T : AclResource> canCreate(aclResource: T) = Deny(AuthorityCheckException("Deny all"))
    override suspend fun canCreateInstanceOf(aclResourceIdentifier: AclResourceIdentifier) = Deny(AuthorityCheckException("Deny all"))

    override suspend fun <T : AclResource> canView(aclResource: T) = Deny(AuthorityCheckException("Deny all"))

    override suspend fun <T : AclResource> canUpdate(aclResource: T) = Deny(AuthorityCheckException("Deny all"))

    override suspend fun <T : AclResource> canDelete(aclResource: T) = Deny(AuthorityCheckException("Deny all"))

    override suspend fun <T : AclResource> filterCanView(entities: Collection<T>) = emptyList<T>()

    override suspend fun ensuring() = DefaultAuthorizedAccountAbilityEnsureProvider(this)
}
