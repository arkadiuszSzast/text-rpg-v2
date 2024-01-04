package com.szastarek.text.rpg.shared.validate

fun String.containsSpecialCharacter(): Boolean {
	return this.contains(Regex("[^A-Za-z0-9 ]"))
}

fun String.containsNumber(): Boolean {
	return this.contains(Regex("[0-9]"))
}
