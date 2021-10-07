package org.dxworks.studio

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.subcommands
import org.dxworks.studio.init.Init
import org.dxworks.studio.ot.Tables
import java.nio.file.Path

fun main(args: Array<String>) {

    Studio().subcommands(Tables(), Init()).main(args)
}

class Studio : CliktCommand() {
    override fun run() = Unit
}