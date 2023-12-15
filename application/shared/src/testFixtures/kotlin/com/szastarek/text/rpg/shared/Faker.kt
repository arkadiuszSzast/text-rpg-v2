package com.szastarek.text.rpg.shared

import com.szastarek.text.rpg.shared.email.EmailAddress
import com.szastarek.text.rpg.shared.password.RawPassword
import com.szastarek.text.rpg.shared.validate.getOrThrow
import io.github.serpro69.kfaker.faker

private val faker = faker { }

fun aRawPassword(value: String = faker.random.randomString(8)) = RawPassword(value).getOrThrow()

fun anEmail(value: String = faker.internet.email()) = EmailAddress(value).getOrThrow()
