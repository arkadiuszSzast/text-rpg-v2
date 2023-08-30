package com.szastarek.text.rpg.acl

import com.szastarek.text.rpg.acl.authority.Authority
import com.szastarek.text.rpg.acl.utils.accountAllowedToModifyOnlyOwnedEntities
import com.szastarek.text.rpg.acl.utils.accountWithAllAuthorities
import com.szastarek.text.rpg.acl.utils.accountWithAllSpecialAuthorities
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class SerializersTest : DescribeSpec({

    describe("Serializers") {


        it("should serialize and deserialize authorities") {
            val json = Json

            //accountWithAllAuthorities
            val encodedAllAuthorities = json.encodeToString(accountWithAllAuthorities.getAuthorities())
            val decodedAllAuthorities = json.decodeFromString<List<Authority>>(encodedAllAuthorities)

            decodedAllAuthorities shouldBe accountWithAllAuthorities.getAuthorities()

            //accountWithAllSpecialAuthorities
            val encodedAllSpecialAuthorities = json.encodeToString(accountWithAllSpecialAuthorities.getAuthorities())
            val decodedAllSpecialAuthorities = json.decodeFromString<List<Authority>>(encodedAllSpecialAuthorities)

            decodedAllSpecialAuthorities shouldBe accountWithAllSpecialAuthorities.getAuthorities()

            //accountAllowedToModifyOnlyOwnedEntities
            val encodedAllowedToModifyOnlyOwnedEntities = json.encodeToString(accountAllowedToModifyOnlyOwnedEntities.getAuthorities())
            val decodedAllowedToModifyOnlyOwnedEntities = json.decodeFromString<List<Authority>>(encodedAllowedToModifyOnlyOwnedEntities)

            decodedAllowedToModifyOnlyOwnedEntities shouldBe accountAllowedToModifyOnlyOwnedEntities.getAuthorities()
        }
    }
})
