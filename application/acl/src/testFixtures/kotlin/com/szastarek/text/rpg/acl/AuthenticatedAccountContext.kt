package com.szastarek.text.rpg.acl

import com.szastarek.text.rpg.acl.authority.Authority
import com.szastarek.text.rpg.shared.email.EmailAddress
import com.szastarek.text.rpg.shared.validate.getOrThrow

val superUserAuthenticatedAccountContext =
	object : AuthenticatedAccountContext {
		override val email: EmailAddress = EmailAddress("super-user@mail.com").getOrThrow()

		override suspend fun getAuthorities(): List<Authority> = role.getAuthorities()

		override val accountId: AccountId = AccountId("super-user")

		override val role: Role = Roles.SuperUser.role
	}

val regularUserAuthenticatedAccountContext =
	object : AuthenticatedAccountContext {
		override val email: EmailAddress = EmailAddress("regular-user@mail.com").getOrThrow()

		override suspend fun getAuthorities(): List<Authority> = role.getAuthorities()

		override val accountId: AccountId = AccountId("regular-user")

		override val role: Role = Roles.RegularUser.role
	}

val worldCreatorAuthenticatedAccountContext =
	object : AuthenticatedAccountContext {
		override val email: EmailAddress = EmailAddress("world-creator@mail.com").getOrThrow()

		override suspend fun getAuthorities(): List<Authority> = role.getAuthorities()

		override val accountId: AccountId = AccountId("world-creator")

		override val role: Role = Roles.WorldCreator.role
	}
