package com.szastarek.text.rpg.shared.plugin

import io.ktor.server.application.ApplicationCall
import io.ktor.server.application.Plugin
import io.ktor.server.application.install
import io.ktor.server.application.pluginRegistry
import io.ktor.util.pipeline.Pipeline

fun <P : Pipeline<*, ApplicationCall>, B : Any, F : Any> P.installIfNotRegistered(
    plugin: Plugin<P, B, F>,
    configure: B.() -> Unit = {}
): F? {
    if (!pluginRegistry.contains(plugin.key)) {
        return install(plugin, configure)
    }
    return null
}
