package me.nobaboy.sentinellink

import me.celestialfault.celestialconfig.AbstractConfig
import me.celestialfault.celestialconfig.Property

object Config : AbstractConfig(SentinelLink.modDir.toPath().resolve("sentinel-link.json")) {
    var firstUsage by Property.boolean("firstUsage", true)
    val tokens by Property.map<String>("tokens")
}