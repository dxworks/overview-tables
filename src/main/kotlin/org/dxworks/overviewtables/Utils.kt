package org.dxworks.overviewtables

import java.io.File

val versionCommandArgs = setOf("-v", "version", "--version", "-version", "-V")

fun writeDefaultConfigFile(resourcePath: String, targetFile: File) =
    object {}::class.java.classLoader.getResourceAsStream(resourcePath)
        ?.let { targetFile.writeBytes(it.readAllBytes()) }

