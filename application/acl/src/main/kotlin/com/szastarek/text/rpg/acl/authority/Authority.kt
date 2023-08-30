package com.szastarek.text.rpg.acl.authority

import com.szastarek.text.rpg.acl.AclResource
import com.szastarek.text.rpg.acl.AclResourceIdentifier
import com.szastarek.text.rpg.acl.Feature
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
sealed interface Authority

@Serializable
@SerialName("FeatureAccessAuthority")
data class FeatureAccessAuthority(val feature: Feature) : Authority

@Serializable
@SerialName("EntityAccessAuthority")
data class EntityAccessAuthority<T : AclResource>(
    val aclResourceIdentifier: AclResourceIdentifier,
    val scopes: List<AuthorityScope<T>>
) : Authority

@Serializable
@SerialName("AllFeaturesAuthority")
data object AllFeaturesAuthority : Authority
@Serializable
@SerialName("ViewAllEntitiesAuthority")
data object ViewAllEntitiesAuthority : Authority
@Serializable
@SerialName("CreateAllEntitiesAuthority")
data object CreateAllEntitiesAuthority : Authority
@Serializable
@SerialName("UpdateAllEntitiesAuthority")
data object UpdateAllEntitiesAuthority : Authority
@Serializable
@SerialName("DeleteAllEntitiesAuthority")
data object DeleteAllEntitiesAuthority : Authority
@Serializable
@SerialName("ManageAllEntitiesAuthority")
data object ManageAllEntitiesAuthority : Authority

fun List<Authority>.hasFeatureAuthority(feature: Feature): Boolean {
    return this.hasAllFeaturesAccessAuthority() ||
            this.filterIsInstance<FeatureAccessAuthority>().any { authority -> authority.feature == feature }
}

fun <T: AclResource> List<Authority>.filterEntityAccessAuthorities(): List<EntityAccessAuthority<T>> =
    this.filterIsInstance<EntityAccessAuthority<T>>()

fun List<Authority>.hasAllFeaturesAccessAuthority(): Boolean =
    any { authority -> authority is AllFeaturesAuthority }

fun List<Authority>.hasReadAllEntitiesAuthority(): Boolean =
    any { authority -> authority is ViewAllEntitiesAuthority || authority is ManageAllEntitiesAuthority }

fun List<Authority>.hasCreateAllEntitiesAuthority(): Boolean =
    any { authority -> authority is CreateAllEntitiesAuthority || authority is ManageAllEntitiesAuthority }

fun List<Authority>.hasUpdateAllEntitiesAuthority(): Boolean =
    any { authority -> authority is UpdateAllEntitiesAuthority || authority is ManageAllEntitiesAuthority }

fun List<Authority>.hasDeleteAllEntitiesAuthority(): Boolean =
    any { authority -> authority is DeleteAllEntitiesAuthority || authority is ManageAllEntitiesAuthority }

fun List<Authority>.mergeWith(overridingAuthorities: List<Authority>): List<Authority> {
    val otherEntityAccessAuthoritiesIdentifiers = overridingAuthorities.filterIsInstance<EntityAccessAuthority<*>>()
        .map { it.aclResourceIdentifier }

    return (this.filter {
        !(it is EntityAccessAuthority<*> && it.aclResourceIdentifier in otherEntityAccessAuthoritiesIdentifiers)
    } + overridingAuthorities).distinct()
}
