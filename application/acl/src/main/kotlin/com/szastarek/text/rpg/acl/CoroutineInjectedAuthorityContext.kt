package com.szastarek.text.rpg.acl

import com.szastarek.text.rpg.acl.authority.Authority
import kotlin.coroutines.AbstractCoroutineContextElement
import kotlin.coroutines.CoroutineContext

data class CoroutineInjectedAuthorityContext(val authorities: List<Authority>) :
    AbstractCoroutineContextElement(CoroutineInjectedAuthorityContext) {

    companion object Key : CoroutineContext.Key<CoroutineInjectedAuthorityContext>
}
