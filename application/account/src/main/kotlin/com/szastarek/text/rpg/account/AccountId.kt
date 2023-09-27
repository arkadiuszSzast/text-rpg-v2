package com.szastarek.text.rpg.account

import com.szastarek.text.rpg.acl.AccountId
import org.litote.kmongo.Id

fun Id<Account>.toAccountId() = AccountId(toString())
