package org.dxworks.overviewtables

import com.github.ajalt.clikt.parameters.options.versionOption
import java.util.*

val version by lazy {
    try {
        Properties().apply { load(object {}::class.java.classLoader.getResourceAsStream("maven.properties")) }["version"] as String
    }catch (e: Exception) {
        "unknown"
    }
}

fun main(args: Array<String>) {

    Tables().versionOption(version, names = versionCommandArgs).main(args)
}