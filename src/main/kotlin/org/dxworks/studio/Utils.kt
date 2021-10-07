package org.dxworks.studio

import java.io.File
import java.nio.file.Path

fun writeDefaultConfigFile(resourcePath: String, targetFile: File) =
    object {}::class.java.classLoader.getResourceAsStream(resourcePath)
        ?.let { targetFile.writeBytes(it.readAllBytes()) }

fun makeDirsIfNecessary(vararg paths: Path) {
    paths.map { it.toFile() }.forEach { it.mkdirs() }
}
