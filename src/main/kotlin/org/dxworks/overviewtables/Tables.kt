package org.dxworks.overviewtables

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.arguments.default
import com.opencsv.CSVReader
import org.apache.poi.openxml4j.util.ZipSecureFile
import org.apache.poi.ss.usermodel.Cell
import org.apache.poi.ss.usermodel.CellType
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.nio.file.Paths
import java.util.*

class Tables :
    CliktCommand(
        name = "dx-tables",
        help = "Converts overview tables csv output to excel file, which will be created in \${dx-folder}/presentation/Overview-Tables.xslx"
    ) {

    private val folderParam by argument(
        "dx-folder",
        help = "The dx home folder (containing settings.txt file)"
    ).default(".")

    override fun run() {
        val folder = File(folderParam).normalize()
        val dxProjName = folder.absoluteFile.name
        println("Project at ${folder.normalize().absolutePath}")
        if (!folder.resolve("settings.txt").exists())
            error("${folder.normalize().absolutePath} is not a dx home folder!")
        val overviewTablesOutput = folder.resolve("+dx-results/overview")
        if (!overviewTablesOutput.exists())
            error("Overview tables output folder ($overviewTablesOutput) does not exist. Please run dx first to create them.")

        val presentationFolder = folder.resolve("presentation")
        presentationFolder.mkdirs()

        val genericResourcesLocation = Paths.get(System.getProperty("user.home"), ".dxw", "tables").toFile()
        genericResourcesLocation.mkdirs()


        val genericOverviewTablesFile = genericResourcesLocation.resolve("Generic-Overview-Tables.xlsx")
        val overviewTablesFile = presentationFolder.resolve("${dxProjName.capitalize()}-Overview-Tables.xlsx")
        if (!genericOverviewTablesFile.exists()) {
            writeDefaultConfigFile("Generic-Overview-Tables.xlsx", genericOverviewTablesFile)
        }
        overviewTablesFile.writeBytes(genericOverviewTablesFile.readBytes())

        val genericOverviewSlidesFile = genericResourcesLocation.resolve("Generic-Overview-Slides.pptx")
        val overviewSlidesFile = presentationFolder.resolve("${dxProjName.capitalize()}-Overview-Slides.pptx")
        if (!genericOverviewSlidesFile.exists()) {
            writeDefaultConfigFile("Generic-Overview-Slides.pptx", genericOverviewSlidesFile)
        }
        overviewSlidesFile.writeBytes(genericOverviewSlidesFile.readBytes())

        ZipSecureFile.setMinInflateRatio(0.0)

        val workbook =
            FileInputStream(overviewTablesFile).use {
                val workbook = XSSFWorkbook(it)

                overviewTablesOutput.listFiles()
                    ?.filter { f -> f.isFile && f.name.endsWith(".csv")}
                    ?.forEach { file ->
                        val csvData: CSVData = CSVReader(file.reader()).use { reader ->
                            val allCsvLines = reader.readAll()
                            val recentDate = allCsvLines.mapNotNull { row ->
                                row.find { cell -> cell.contains("recent refers to") }?.substringAfter("since ")
                            }.firstOrNull()
                            val dataRows = allCsvLines.dropWhile { strings -> strings[0].toIntOrNull() == null }
                                .associate { strings -> strings[0].toInt() to strings.drop(1).toList() }.toSortedMap()

                            CSVData(recentDate, dataRows)
                        }

                        workbook.getSheet(file.name.removeSuffix(".csv"))?.let { sheet ->
                            sheet.take(5)
                                .flatMap { row ->
                                    row.filter { cell ->
                                        cell.cellType == CellType.STRING &&
                                                cell.stringCellValue.contains("\$recentDate")
                                    }
                                }
                                .forEach { cell ->
                                    cell.setCellValue(
                                        cell.stringCellValue.replace(
                                            "\$recentDate", csvData.recentDate.orEmpty()
                                        )
                                    )
                                }

                            val dataRows =
                                sheet.dropWhile { row -> row.count() == 0 || !row.first().stringCellValue.startsWith("spring") }
                                    .withIndex()
                                    .associate { indexedValue -> (indexedValue.index + 1) to indexedValue.value }
                                    .toSortedMap()

                            dataRows.forEach { (index, row) ->
                                val dataFromCSV = csvData.dataRows[index]
                                when {
                                    dataFromCSV == null -> sheet.removeRow(row)
                                    dataFromCSV.size == 1 -> row.drop(1).forEach(Cell::setBlank)
                                    else -> {
                                        dataFromCSV.forEachIndexed { colIndex, value ->
                                            val cell = row.getCell(colIndex)
                                            try {
                                                cell.setCellValue(value.toDouble())
                                            } catch (e: NumberFormatException) {
                                                cell.setCellValue(value)
                                            }
                                        }
                                        row.drop(dataFromCSV.size).forEach(Cell::setBlank)
                                    }
                                }
                            }
                        }
                    }
                workbook
            }

        FileOutputStream(overviewTablesFile).use {
            workbook.write(it)
            workbook.close()
        }

        println("Finished. Results can be found at ${overviewTablesFile.absolutePath}")
        println("If you want to modify the generic template files they can be found at ${genericResourcesLocation.absolutePath}")
    }

    class CSVData(
        val recentDate: String?,
        val dataRows: SortedMap<Int, List<String>>
    )

}