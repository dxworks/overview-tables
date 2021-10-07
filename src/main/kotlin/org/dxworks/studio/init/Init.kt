package org.dxworks.studio.init

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.arguments.argument
import org.dxworks.studio.makeDirsIfNecessary
import java.nio.file.Paths

class Init : CliktCommand() {

    val projectID by argument(help = "The id of the project you want to create")

    override fun run() {
        val chronosFolder = Paths.get("chronos")

        val chronos1DataFolder = Paths.get("chronos", "data", "chronos")
        val chronosDataFolder = Paths.get("chronos", "data", "chronos2")

        makeDirsIfNecessary(chronosFolder, chronos1DataFolder, chronosDataFolder)

        val envFile = Paths.get("chronos", ".env")
        val dockerComposeFile = Paths.get("chronos", "docker-compose.yml")
    }

}