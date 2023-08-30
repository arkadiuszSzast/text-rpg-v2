package com.szastarek.text.rpg.acl

import kotlin.coroutines.AbstractCoroutineContextElement
import kotlin.coroutines.CoroutineContext

data class CoroutineAccountContext(val accountContext: AccountContext) :
    AbstractCoroutineContextElement(CoroutineAccountContext) {

    companion object Key : CoroutineContext.Key<CoroutineAccountContext>
}
