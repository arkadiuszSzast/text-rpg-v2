package com.szastarek.text.rpg.account

import org.litote.kmongo.Id

interface Account {
    val id: Id<Account>
}
