package com.szastarek.text.rpg.acl.authority

interface AuthoritiesProvider {
    suspend fun getAuthorities(): List<Authority>
}
