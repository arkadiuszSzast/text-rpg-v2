package com.szastarek.text.rpg.acl

import com.szastarek.text.rpg.acl.authority.Authority
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.withContext

suspend fun <T> withInjectedAuthorities(authorities: List<Authority>, block: suspend () -> T): T {
    val currentInjectedAuthorities = currentCoroutineContext()[CoroutineInjectedAuthorityContext]?.authorities ?: emptyList()
    val authoritiesToInject = (authorities + currentInjectedAuthorities).distinct()

    return withContext(currentCoroutineContext() + CoroutineInjectedAuthorityContext(authoritiesToInject)) {
        block()
    }
}
