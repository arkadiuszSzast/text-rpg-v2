package com.szastarek.text.rpg.acl.authority

import com.szastarek.text.rpg.acl.AclResource
import com.szastarek.text.rpg.acl.AclResourceIdentifier
import com.szastarek.text.rpg.acl.Feature

interface AuthorizedAccountAbilityEnsureProvider {
    suspend fun ensureHasAccessTo(feature: Feature)

    suspend fun <T : AclResource> ensureCanCreate(aclResource: T)

    suspend fun ensureCanCreateInstanceOf(aclResourceIdentifier: AclResourceIdentifier)

    suspend fun <T : AclResource> ensureCanUpdate(aclResource: T)

    suspend fun <T : AclResource> ensureCanDelete(aclResource: T)

    suspend fun <T : AclResource> ensureCanView(aclResource: T)

    suspend fun <T : AclResource> filterCanView(entities: Collection<T>): Collection<T>
}
