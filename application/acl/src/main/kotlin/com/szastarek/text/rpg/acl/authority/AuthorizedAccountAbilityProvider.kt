package com.szastarek.text.rpg.acl.authority

import com.szastarek.text.rpg.acl.AclResource
import com.szastarek.text.rpg.acl.AclResourceIdentifier
import com.szastarek.text.rpg.acl.Feature

interface AuthorizedAccountAbilityProvider {
    suspend fun hasAccessTo(feature: Feature): Decision

    suspend fun <T : AclResource> canCreate(aclResource: T): Decision

    suspend fun canCreateInstanceOf(aclResourceIdentifier: AclResourceIdentifier): Decision

    suspend fun <T : AclResource> canView(aclResource: T): Decision

    suspend fun <T : AclResource> canUpdate(aclResource: T): Decision

    suspend fun <T : AclResource> canDelete(aclResource: T): Decision

    suspend fun <T : AclResource> filterCanView(entities: Collection<T>): Collection<T>

    suspend fun ensuring(): AuthorizedAccountAbilityEnsureProvider
}
