package com.szastarek.text.rpg.shared.config

import com.typesafe.config.ConfigFactory
import io.ktor.server.config.HoconApplicationConfig

object ConfigMap {
    val config by lazy { HoconApplicationConfig(ConfigFactory.load()) }
}

fun getStringProperty(key: ConfigKey) = ConfigMap.config.property(key.key).getString()

fun getLongProperty(key: ConfigKey) = ConfigMap.config.property(key.key).getString().toLong()

fun getBooleanProperty(key: ConfigKey) = ConfigMap.config.property(key.key).getString().toBoolean()

fun getListProperty(key: ConfigKey) = ConfigMap.config.property(key.key).getList()

@JvmInline
value class ConfigKey(val key: String) {
    operator fun plus(other: ConfigKey) = ConfigKey("$key.${other.key}")
}
