package com.szastarek.text.rpg.acl

import com.szastarek.text.rpg.acl.authority.Allow
import com.szastarek.text.rpg.acl.authority.AuthorizedAccountAbilityProvider
import com.szastarek.text.rpg.acl.authority.DefaultAuthorizedAccountAbilityEnsureProvider

class CanDoAnythingAuthorizedAccountAbilityProvider : AuthorizedAccountAbilityProvider {
    override suspend fun hasAccessTo(feature: Feature) = Allow

    override suspend fun <T : AclResource> canCreate(aclResource: T) = Allow

    override suspend fun canCreateInstanceOf(aclResourceIdentifier: AclResourceIdentifier) = Allow

    override suspend fun <T : AclResource> canView(aclResource: T) = Allow

    override suspend fun <T : AclResource> canUpdate(aclResource: T) = Allow

    override suspend fun <T : AclResource> canDelete(aclResource: T) = Allow

    override suspend fun <T : AclResource> filterCanView(entities: Collection<T>) = entities

    override suspend fun ensuring() = DefaultAuthorizedAccountAbilityEnsureProvider(this)
}
