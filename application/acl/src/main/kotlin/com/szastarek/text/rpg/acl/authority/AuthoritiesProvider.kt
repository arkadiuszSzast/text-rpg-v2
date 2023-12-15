package com.szastarek.text.rpg.acl.authority

fun interface AuthoritiesProvider {
	suspend fun getAuthorities(): List<Authority>
}
