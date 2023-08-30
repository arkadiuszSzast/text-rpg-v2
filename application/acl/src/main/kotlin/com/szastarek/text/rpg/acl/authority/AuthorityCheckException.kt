package com.szastarek.text.rpg.acl.authority

data class AuthorityCheckException(override val message: String) : RuntimeException(message)
