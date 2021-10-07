package org.dxworks.studio

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.subcommands
import com.github.ajalt.clikt.parameters.options.versionOption
import org.dxworks.studio.init.Init
import org.dxworks.studio.ot.Tables

fun main(args: Array<String>) {

    Studio().versionOption("0.1.0", names = versionCommandArgs).subcommands(Tables(), Init()).main(args)
}

class Studio : CliktCommand(name = "chronos-studio") {
    override fun run() = Unit
}