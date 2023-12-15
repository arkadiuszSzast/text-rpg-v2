package com.szastarek.text.rpg.acl.utils

import com.szastarek.text.rpg.acl.AccountContext
import com.szastarek.text.rpg.acl.AccountContextProvider
import com.szastarek.text.rpg.acl.AccountId
import com.szastarek.text.rpg.acl.AclResource
import com.szastarek.text.rpg.acl.AclResourceIdentifier
import com.szastarek.text.rpg.acl.AuthenticatedAccountContext
import com.szastarek.text.rpg.acl.BelongsToAccount
import com.szastarek.text.rpg.acl.Feature
import com.szastarek.text.rpg.acl.RegularRole
import com.szastarek.text.rpg.acl.Role
import com.szastarek.text.rpg.acl.SuperUserRole
import com.szastarek.text.rpg.acl.authority.AclResourceBelongsToAccountPredicate
import com.szastarek.text.rpg.acl.authority.Authority
import com.szastarek.text.rpg.acl.authority.authorities
import com.szastarek.text.rpg.shared.email.EmailAddress
import com.szastarek.text.rpg.shared.validate.getOrThrow

data class StaticAccountContextProvider(private val accountContext: AccountContext) : AccountContextProvider {
    override suspend fun currentContext(): AccountContext {
        return accountContext
    }
}

val superUserAccount = object : AuthenticatedAccountContext {

    override val accountId: AccountId = AccountId("super-user-account")

    override val email: EmailAddress = EmailAddress("super-user@mail.com").getOrThrow()

    override suspend fun getAuthorities(): List<Authority> = emptyList()

    override val role: Role = SuperUserRole
}

val accountWithoutAuthorities = object : AuthenticatedAccountContext {

    override val accountId: AccountId = AccountId("no-authorities-account")

    override val email: EmailAddress = EmailAddress("no-authorities@mail.com").getOrThrow()

    override suspend fun getAuthorities(): List<Authority> = emptyList()

    override val role: Role = RegularRole("regular-user", emptyList())
}

val accountWithAllAuthorities = object : AuthenticatedAccountContext {

    override val accountId: AccountId = AccountId("all-authorities-account")

    override val email: EmailAddress = EmailAddress("all-authorities@mail.com").getOrThrow()

    override suspend fun getAuthorities(): List<Authority> = authorities {
        featureAccess(sendEmailFeature)
        entityAccess<Dog>(Dog.aclResourceIdentifier) {
            createScope()
            viewScope()
            updateScope()
            deleteScope()
        }
    }

    override val role: Role = RegularRole("regular-user", emptyList())
}

val accountWithManageAuthority = object : AuthenticatedAccountContext {

    override val accountId: AccountId = AccountId("manage-authority-account")

    override val email: EmailAddress = EmailAddress("managed-authorities@mail.com").getOrThrow()

    override suspend fun getAuthorities(): List<Authority> = authorities {
        featureAccess(sendEmailFeature)
        entityAccess<Dog>(Dog.aclResourceIdentifier) {
            manageScope()
        }
    }

    override val role: Role = RegularRole("regular-user", emptyList())
}

val accountWithAllSpecialAuthorities = object : AuthenticatedAccountContext {

    override val accountId: AccountId = AccountId("all-special-authorities-account")

    override val email: EmailAddress = EmailAddress("special-authorities@mail.com").getOrThrow()

    override suspend fun getAuthorities(): List<Authority> = authorities {
        featureAccess(sendEmailFeature)
        viewAllEntitiesAuthority()
        createAllEntitiesAuthority()
        updateAllEntitiesAuthority()
        deleteAllEntitiesAuthority()
        manageAllEntitiesAuthority()
        allFeaturesAuthority()
    }

    override val role: Role = RegularRole("regular-user", emptyList())
}

val accountAllowedToModifyOnlyOwnedEntities = object : AuthenticatedAccountContext {

    override val accountId: AccountId = AccountId("modify-only-owned-entities-account")

    override val email: EmailAddress = EmailAddress("owned-authorities@mail.com").getOrThrow()

    override suspend fun getAuthorities(): List<Authority> = authorities {
        featureAccess(sendEmailFeature)
        entityAccess<Dog>(Dog.aclResourceIdentifier) {
            createScope(AclResourceBelongsToAccountPredicate())
            viewScope(AclResourceBelongsToAccountPredicate())
            updateScope(AclResourceBelongsToAccountPredicate())
            deleteScope(AclResourceBelongsToAccountPredicate())
        }
    }

    override val role: Role = RegularRole("regular-user", emptyList())
}

data class Dog(val name: String, val age: Int, override val accountId: AccountId) : AclResource, BelongsToAccount {

    companion object {
        val aclResourceIdentifier = AclResourceIdentifier("Dog")

        fun default() = Dog("Burek", 5, AccountId("account-1"))

        fun ofAccount(accountId: AccountId) = Dog("Burek", 5, accountId)
    }

    override val aclResourceIdentifier: AclResourceIdentifier
        get() = Companion.aclResourceIdentifier
}

val sendEmailFeature = Feature("some-feature")
