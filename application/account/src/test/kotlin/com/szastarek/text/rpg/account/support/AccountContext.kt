package com.szastarek.text.rpg.account.support

import com.szastarek.text.rpg.account.event.AccountCreatedEvent
import com.szastarek.text.rpg.account.toAccountId
import com.szastarek.text.rpg.acl.AccountId
import com.szastarek.text.rpg.acl.AuthenticatedAccountContext
import com.szastarek.text.rpg.acl.Role
import com.szastarek.text.rpg.acl.authority.Authority
import com.szastarek.text.rpg.acl.authority.mergeWith
import com.szastarek.text.rpg.acl.getAuthorities
import com.szastarek.text.rpg.shared.email.EmailAddress

fun AccountCreatedEvent.toAccountContext() = object : AuthenticatedAccountContext {
  override val email: EmailAddress = this@toAccountContext.emailAddress

  override suspend fun getAuthorities(): List<Authority> = this@toAccountContext.role.getAuthorities()
    .mergeWith(this@toAccountContext.customAuthorities)

  override val accountId: AccountId = this@toAccountContext.accountId.toAccountId()

  override val role: Role = this@toAccountContext.role
}