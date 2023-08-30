package com.szastarek.text.rpg.acl.authority

import com.szastarek.text.rpg.acl.utils.Dog
import com.szastarek.text.rpg.acl.utils.StaticAccountContextProvider
import com.szastarek.text.rpg.acl.utils.accountWithAllAuthorities
import com.szastarek.text.rpg.acl.utils.accountWithoutAuthorities
import com.szastarek.text.rpg.acl.utils.sendEmailFeature
import io.kotest.assertions.assertSoftly
import io.kotest.assertions.throwables.shouldNotThrowAny
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import com.szastarek.text.rpg.acl.authority.DefaultAuthorizedAccountAbilityProvider as AccountAbilityProvider

class DefaultAuthorizedAccountAbilityEnsureProviderTest : DescribeSpec({

    describe("DefaultAuthorizedAccountAbilityEnsureProviderTest") {

        it("should not throw exception on allow") {
            //arrange
            val acl = AccountAbilityProvider(StaticAccountContextProvider(accountWithAllAuthorities)).ensuring()

            //act & assert
            assertSoftly {
                shouldNotThrowAny { acl.ensureCanCreateInstanceOf(Dog.aclResourceIdentifier) }
                shouldNotThrowAny { acl.ensureCanCreate(Dog.default()) }
                shouldNotThrowAny { acl.ensureCanView(Dog.default()) }
                shouldNotThrowAny { acl.ensureCanUpdate(Dog.default()) }
                shouldNotThrowAny { acl.ensureCanDelete(Dog.default()) }
                shouldNotThrowAny { acl.ensureHasAccessTo(sendEmailFeature) }
            }
        }

        it("should throw exception on deny") {
            //arrange
            val acl = AccountAbilityProvider(StaticAccountContextProvider(accountWithoutAuthorities)).ensuring()

            //act & assert
            assertSoftly {
                shouldThrow<AuthorityCheckException>{ acl.ensureCanCreateInstanceOf(Dog.aclResourceIdentifier) }
                shouldThrow<AuthorityCheckException>{ acl.ensureCanCreate(Dog.default()) }
                shouldThrow<AuthorityCheckException>{ acl.ensureCanView(Dog.default()) }
                shouldThrow<AuthorityCheckException>{ acl.ensureCanUpdate(Dog.default()) }
                shouldThrow<AuthorityCheckException>{ acl.ensureCanDelete(Dog.default()) }
                shouldThrow<AuthorityCheckException>{ acl.ensureHasAccessTo(sendEmailFeature) }
            }
        }
    }
})
