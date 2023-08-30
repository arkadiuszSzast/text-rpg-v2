package com.szastarek.text.rpg.acl

enum class Roles(val code: String, val role: Role) {
    SuperUser("SUPER_USER", SuperUserRole),
    RegularUser("REGULAR_USER", RegularRole("REGULAR_USER", listOf()));

    companion object {
        fun getByCode(code: String): Role {
            return entries.single { it.code == code }.role
        }

        fun getByRole(role: Role): String {
            return entries.single { it.role == role }.code
        }
    }
}
