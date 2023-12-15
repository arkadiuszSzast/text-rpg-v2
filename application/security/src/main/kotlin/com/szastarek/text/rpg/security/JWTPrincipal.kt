package com.szastarek.text.rpg.security

import com.auth0.jwt.exceptions.JWTDecodeException
import com.szastarek.text.rpg.acl.AccountId
import com.szastarek.text.rpg.acl.Roles
import com.szastarek.text.rpg.acl.authority.Authority
import com.szastarek.text.rpg.shared.email.EmailAddress
import io.ktor.server.auth.jwt.JWTPrincipal
import kotlinx.serialization.json.Json

val JWTPrincipal.accountId
	get() = this.subject?.let { AccountId(it) } ?: throw JWTDecodeException("Claim accountId is missing")

val JWTPrincipal.emailAddress
	get() = this["email"]?.let { EmailAddress(it).getOrNull() } ?: throw JWTDecodeException("Claim email is missing")

val JWTPrincipal.role
	get() = this["role"]?.let { Roles.getByCode(it) } ?: throw JWTDecodeException("Claim role is missing")

val JWTPrincipal.customAuthorities
	get() =
		this["custom_authorities"]?.let { Json.decodeFromString<List<Authority>>(it) }
			?: throw JWTDecodeException("Claim custom_authorities is missing")
