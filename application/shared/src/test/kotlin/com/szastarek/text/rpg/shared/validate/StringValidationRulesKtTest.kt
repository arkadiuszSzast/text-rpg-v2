package com.szastarek.text.rpg.shared.validate

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe

class StringValidationRulesKtTest : StringSpec({

	"should find special characters in string" {
		"%abc^de".containsSpecialCharacter() shouldBe true
	}

	"should not find any special character in string" {
		"test".containsSpecialCharacter() shouldBe false
	}

	"should find number in string" {
		"abc123".containsNumber() shouldBe true
	}

	"should not find any number in string" {
		"abc".containsNumber() shouldBe false
	}
})
