package com.szastarek.text.rpg.acl

import com.szastarek.text.rpg.acl.authority.AclResourceBelongsToAccountPredicate
import com.szastarek.text.rpg.acl.authority.authorities

enum class Roles(val code: String, val role: Role) {
	SuperUser("SUPER_USER", SuperUserRole),
	RegularUser(
		"REGULAR_USER",
		RegularRole(
			"REGULAR_USER",
			authorities {
				entityAccess(AclResourceIdentifier("account-aggregate")) {
					manageScope(AclResourceBelongsToAccountPredicate())
				}
			},
		),
	),
	WorldCreator(
		"WORLD_CREATOR",
		RegularRole(
			"WORDL_CREATOR",
			authorities {
				entityAccess(AclResourceIdentifier("account-aggregate")) {
					manageScope(AclResourceBelongsToAccountPredicate())
				}
			},
		),
	),
	;

	companion object {
		fun getByCode(code: String): Role {
			return entries.single { it.code == code }.role
		}

		fun getByRole(role: Role): String {
			return entries.single { it.role == role }.code
		}
	}
}
