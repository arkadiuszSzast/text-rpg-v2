package com.szastarek.text.rpg.acl.authority

import com.szastarek.text.rpg.acl.AccountId
import com.szastarek.text.rpg.acl.utils.Dog
import com.szastarek.text.rpg.acl.utils.StaticAccountContextProvider
import com.szastarek.text.rpg.acl.utils.accountAllowedToModifyOnlyOwnedEntities
import com.szastarek.text.rpg.acl.utils.accountWithAllAuthorities
import com.szastarek.text.rpg.acl.utils.accountWithAllSpecialAuthorities
import com.szastarek.text.rpg.acl.utils.accountWithManageAuthority
import com.szastarek.text.rpg.acl.utils.accountWithoutAuthorities
import com.szastarek.text.rpg.acl.utils.sendEmailFeature
import com.szastarek.text.rpg.acl.utils.superUserAccount
import io.kotest.assertions.assertSoftly
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import com.szastarek.text.rpg.acl.authority.DefaultAuthorizedAccountAbilityProvider as AccountAbilityProvider

class DefaultAuthorizedAccountAbilityProviderTest : DescribeSpec({

	describe("DefaultAuthorizedAccountAbilityProvider") {

		describe("feature access") {

			it("should allow when super-user") {
				// arrange
				val acl = AccountAbilityProvider(StaticAccountContextProvider(superUserAccount))

				// act & assert
				acl.hasAccessTo(sendEmailFeature) shouldBe Allow
			}

			it("should allow when all features authority given") {
				// arrange
				val acl = AccountAbilityProvider(StaticAccountContextProvider(accountWithAllSpecialAuthorities))

				// act & assert
				acl.hasAccessTo(sendEmailFeature) shouldBe Allow
			}

			it("should allow when concrete feature authority given") {
				// arrange
				val acl = AccountAbilityProvider(StaticAccountContextProvider(accountWithAllAuthorities))

				// act & assert
				acl.hasAccessTo(sendEmailFeature) shouldBe Allow
			}

			it("should deny when feature authority not given") {
				// arrange
				val acl = AccountAbilityProvider(StaticAccountContextProvider(accountWithoutAuthorities))

				// act & assert
				acl.hasAccessTo(sendEmailFeature).shouldBeInstanceOf<Deny>()
			}
		}

		describe("entity access authority") {

			it("should allow when super-user") {
				// arrange
				val acl = AccountAbilityProvider(StaticAccountContextProvider(superUserAccount))

				// act & assert
				assertSoftly {
					acl.canCreateInstanceOf(Dog.aclResourceIdentifier) shouldBe Allow
					acl.canCreate(Dog.default()) shouldBe Allow
					acl.canView(Dog.default()) shouldBe Allow
					acl.canUpdate(Dog.default()) shouldBe Allow
					acl.canDelete(Dog.default()) shouldBe Allow
				}
			}

			it("should allow when all scope authorities given") {
				// arrange
				val acl = AccountAbilityProvider(StaticAccountContextProvider(accountWithAllSpecialAuthorities))

				// act & assert
				assertSoftly {
					acl.canCreateInstanceOf(Dog.aclResourceIdentifier) shouldBe Allow
					acl.canCreate(Dog.default()) shouldBe Allow
					acl.canView(Dog.default()) shouldBe Allow
					acl.canUpdate(Dog.default()) shouldBe Allow
					acl.canDelete(Dog.default()) shouldBe Allow
				}
			}

			it("should allow when concrete scope authority given") {
				// arrange
				val acl = AccountAbilityProvider(StaticAccountContextProvider(accountWithAllAuthorities))

				// act & assert
				assertSoftly {
					acl.canCreateInstanceOf(Dog.aclResourceIdentifier) shouldBe Allow
					acl.canCreate(Dog.default()) shouldBe Allow
					acl.canView(Dog.default()) shouldBe Allow
					acl.canUpdate(Dog.default()) shouldBe Allow
					acl.canDelete(Dog.default()) shouldBe Allow
				}
			}

			it("should allow when manage scope given") {
				// arrange
				val acl = AccountAbilityProvider(StaticAccountContextProvider(accountWithManageAuthority))

				// act & assert
				assertSoftly {
					acl.canCreateInstanceOf(Dog.aclResourceIdentifier) shouldBe Allow
					acl.canCreate(Dog.default()) shouldBe Allow
					acl.canView(Dog.default()) shouldBe Allow
					acl.canUpdate(Dog.default()) shouldBe Allow
					acl.canDelete(Dog.default()) shouldBe Allow
				}
			}

			it("should deny when scope authority not given") {
				// arrange
				val acl = AccountAbilityProvider(StaticAccountContextProvider(accountWithoutAuthorities))

				// act & assert
				assertSoftly {
					acl.canCreateInstanceOf(Dog.aclResourceIdentifier).shouldBeInstanceOf<Deny>()
					acl.canCreate(Dog.default()).shouldBeInstanceOf<Deny>()
					acl.canView(Dog.default()).shouldBeInstanceOf<Deny>()
					acl.canUpdate(Dog.default()).shouldBeInstanceOf<Deny>()
					acl.canDelete(Dog.default()).shouldBeInstanceOf<Deny>()
				}
			}

			it("should allow when predicate satisfied") {
				// arrange
				val acl = AccountAbilityProvider(StaticAccountContextProvider(accountAllowedToModifyOnlyOwnedEntities))
				val dog = Dog.ofAccount(accountAllowedToModifyOnlyOwnedEntities.accountId)

				// act & assert
				assertSoftly {
					acl.canCreate(dog) shouldBe Allow
					acl.canView(dog) shouldBe Allow
					acl.canUpdate(dog) shouldBe Allow
					acl.canDelete(dog) shouldBe Allow
				}
			}

			it("should deny when predicate not satisfied") {
				// arrange
				val acl = AccountAbilityProvider(StaticAccountContextProvider(accountAllowedToModifyOnlyOwnedEntities))
				val dog = Dog.ofAccount(AccountId("another-account"))

				// act & assert
				assertSoftly {
					acl.canCreate(dog).shouldBeInstanceOf<Deny>()
					acl.canView(dog).shouldBeInstanceOf<Deny>()
					acl.canUpdate(dog).shouldBeInstanceOf<Deny>()
					acl.canDelete(dog).shouldBeInstanceOf<Deny>()
				}
			}

			it("should filter only accessible entities") {
				// arrange
				val acl = AccountAbilityProvider(StaticAccountContextProvider(accountAllowedToModifyOnlyOwnedEntities))
				val list =
					listOf(
						Dog.ofAccount(accountAllowedToModifyOnlyOwnedEntities.accountId),
						Dog.ofAccount(AccountId("another-account")),
					)
				val expected = listOf(Dog.ofAccount(accountAllowedToModifyOnlyOwnedEntities.accountId))

				// act && assert
				acl.filterCanView(list) shouldBe expected
			}
		}
	}
})
