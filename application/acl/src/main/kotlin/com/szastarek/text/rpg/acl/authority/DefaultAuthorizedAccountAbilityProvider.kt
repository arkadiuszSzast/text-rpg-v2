package com.szastarek.text.rpg.acl.authority

import com.szastarek.text.rpg.acl.AccountContextProvider
import com.szastarek.text.rpg.acl.AclResource
import com.szastarek.text.rpg.acl.AclResourceIdentifier
import com.szastarek.text.rpg.acl.AnonymousAccountContext
import com.szastarek.text.rpg.acl.AuthenticatedAccountContext
import com.szastarek.text.rpg.acl.Feature
import com.szastarek.text.rpg.acl.SuperUserRole
import io.github.oshai.kotlinlogging.KotlinLogging

class DefaultAuthorizedAccountAbilityProvider(
    private val accountContextProvider: AccountContextProvider
) : AuthorizedAccountAbilityProvider {
    private val logger = KotlinLogging.logger {}

    override suspend fun hasAccessTo(feature: Feature): Decision {
        val hasAccessToFeature = accountContextProvider.currentContext().getAuthorities()
            .hasFeatureAuthority(feature)

        if (!hasAccessToFeature && !isSuperUser()) {
            val message = refusalFeatureMessage(feature)
            logger.warn { message }
            return Deny(AuthorityCheckException(message))
        }
        return Allow
    }

    override suspend fun <T : AclResource> canCreate(aclResource: T): Decision {
        val accountContext = accountContextProvider.currentContext()
        val authorities = accountContext.getAuthorities()
        val hasCreateAllEntitiesAuthority = authorities.hasCreateAllEntitiesAuthority()

        val canCreate = authorities
            .filterEntityAccessAuthorities<T>()
            .find { it.aclResourceIdentifier == aclResource.aclResourceIdentifier }
            ?.scopes
            ?.filter { it.level == AuthorityLevel.Create || it.level == AuthorityLevel.Manage }
            ?.any { scope -> scope.predicates.all { it.isSatisfiedBy(aclResource, accountContext) } } ?: false

        if (!canCreate && !isSuperUser() && !hasCreateAllEntitiesAuthority) {
            val message = refusalMessage(AuthorityLevel.Create, aclResource.aclResourceIdentifier)
            logger.warn { message }
            return Deny(AuthorityCheckException(message))
        }
        return Allow
    }

    override suspend fun canCreateInstanceOf(aclResourceIdentifier: AclResourceIdentifier): Decision {
        val authorities = accountContextProvider.currentContext().getAuthorities()

        val hasCreateAllEntitiesAuthority = authorities.hasCreateAllEntitiesAuthority()

        val canCreate = authorities
            .filterEntityAccessAuthorities<AclResource>()
            .find { it.aclResourceIdentifier == aclResourceIdentifier }
            ?.scopes
            ?.filter { it.level == AuthorityLevel.Create || it.level == AuthorityLevel.Manage } != null

        if (!canCreate && !isSuperUser() && !hasCreateAllEntitiesAuthority) {
            val message = refusalMessage(AuthorityLevel.Create, aclResourceIdentifier)
            logger.warn { message }
            return Deny(AuthorityCheckException(message))
        }
        return Allow
    }

    override suspend fun <T : AclResource> canView(aclResource: T): Decision {
        val accountContext = accountContextProvider.currentContext()
        val authorities = accountContext.getAuthorities()
        val hasReadAllEntitiesAuthority = authorities.hasReadAllEntitiesAuthority()

        val canView = authorities
            .filterEntityAccessAuthorities<T>()
            .find { it.aclResourceIdentifier == aclResource.aclResourceIdentifier }
            ?.scopes
            ?.filter { it.level == AuthorityLevel.View || it.level == AuthorityLevel.Manage }
            ?.any { scope -> scope.predicates.all { it.isSatisfiedBy(aclResource, accountContext) } } ?: false

        if (!canView && !isSuperUser() && !hasReadAllEntitiesAuthority) {
            val message = refusalMessage(AuthorityLevel.View, aclResource.aclResourceIdentifier)
            logger.warn { message }
            return Deny(AuthorityCheckException(message))
        }
        return Allow
    }

    override suspend fun <T : AclResource> canUpdate(aclResource: T): Decision {
        val accountContext = accountContextProvider.currentContext()
        val authorities = accountContext.getAuthorities()
        val hasUpdateAllEntitiesAuthority = authorities.hasUpdateAllEntitiesAuthority()

        val canUpdate = authorities
            .filterEntityAccessAuthorities<T>()
            .find { it.aclResourceIdentifier == aclResource.aclResourceIdentifier }
            ?.scopes
            ?.filter { it.level == AuthorityLevel.Update || it.level == AuthorityLevel.Manage }
            ?.any { scope -> scope.predicates.all { it.isSatisfiedBy(aclResource, accountContext) } } ?: false

        if (!canUpdate && !isSuperUser() && !hasUpdateAllEntitiesAuthority) {
            val message = refusalMessage(AuthorityLevel.Update, aclResource.aclResourceIdentifier)
            logger.warn { message }
            return Deny(AuthorityCheckException(message))
        }
        return Allow
    }

    override suspend fun <T : AclResource> canDelete(aclResource: T): Decision {
        val accountContext = accountContextProvider.currentContext()
        val authorities = accountContext.getAuthorities()
        val hasDeleteAllEntitiesAuthority = authorities.hasDeleteAllEntitiesAuthority()

        val canDelete = authorities
            .filterEntityAccessAuthorities<T>()
            .find { it.aclResourceIdentifier == aclResource.aclResourceIdentifier }
            ?.scopes
            ?.filter { it.level == AuthorityLevel.Delete || it.level == AuthorityLevel.Manage }
            ?.any { scope -> scope.predicates.all { it.isSatisfiedBy(aclResource, accountContext) } } ?: false

        if (!canDelete && !isSuperUser() && !hasDeleteAllEntitiesAuthority) {
            val message = refusalMessage(AuthorityLevel.Delete, aclResource.aclResourceIdentifier)
            logger.warn { message }
            return Deny(AuthorityCheckException(message))
        }
        return Allow
    }

    override suspend fun <T : AclResource> filterCanView(entities: Collection<T>): Collection<T> {
        return entities.filter { canView(it).toBoolean() }
    }

    private suspend fun isSuperUser(): Boolean {
        val principal = accountContextProvider.currentContext()
        return principal is AuthenticatedAccountContext && principal.role is SuperUserRole
    }
    private suspend fun refusalMessage(level: AuthorityLevel, aclResourceIdentifier: AclResourceIdentifier): String {
        return when(val principal = accountContextProvider.currentContext()) {
            is AuthenticatedAccountContext -> {
                "Account with id: [${principal.accountId}] cannot perform $level action " +
                        "on $aclResourceIdentifier resource."
            }

            is AnonymousAccountContext -> {
                "Anonymous account cannot perform $level action " +
                        "on $aclResourceIdentifier resource."
            }
        }
    }

    private suspend fun refusalFeatureMessage(feature: Feature): String {
        return when(val principal = accountContextProvider.currentContext()) {
            is AuthenticatedAccountContext -> {
                "Account with id: [${principal.accountId}] has no access to $feature feature."
            }

            is AnonymousAccountContext -> {
                "Anonymous account has no access to $feature feature."
            }
        }
    }

    override suspend fun ensuring() = DefaultAuthorizedAccountAbilityEnsureProvider(this)
}
