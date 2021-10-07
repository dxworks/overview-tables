package org.dxworks.studio.ot

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.arguments.default
import com.opencsv.CSVReader
import org.apache.poi.openxml4j.util.ZipSecureFile
import org.apache.poi.ss.usermodel.Cell
import org.apache.poi.ss.usermodel.CellType
import org.apache.poi.xslf.usermodel.XMLSlideShow
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import org.dxworks.studio.writeDefaultConfigFile
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.*

class Tables :
    CliktCommand(help = "Converts overview tables csv output to excel file, which will be created in \${dx-folder}/presentation/Overview-Tables.xslx") {

    val folderParam by argument(
        "dx-folder",
        help = "The dx home folder (containing settings.txt file)"
    ).default(".")

    override fun run() {
        val folder = File(folderParam)
        val dxProjName = folder.name
        println("Project at ${folder.normalize().absolutePath}")
        if (!folder.resolve("settings.txt")
                .exists()
        ) error("${folder.normalize().absolutePath} is not a dx home folder!")
        val overviewTablesOutput = folder.resolve("+dx-results/overview")
        if (!overviewTablesOutput.exists())
            error("Overview tables output folder ($overviewTablesOutput) does not exist. Please run dx first to create them.")

        val presentationFolder = folder.resolve("presentation")
        presentationFolder.mkdirs()

        val overviewTablesFile = presentationFolder.resolve("${dxProjName.capitalize()}-Overview-Tables.xlsx")
        writeDefaultConfigFile("tables/Generic-Overview-Tables.xlsx", overviewTablesFile)

        val overviewSlidesFile = presentationFolder.resolve("${dxProjName.capitalize()}-Overview-Slides.pptx")
        writeDefaultConfigFile("tables/Generic-Overview-Slides.pptx", overviewSlidesFile)

        ZipSecureFile.setMinInflateRatio(0.0)

        val slides: XMLSlideShow =
            FileInputStream(overviewSlidesFile).use {
                val slides = XMLSlideShow(it)



                slides
            }

        val workbook =
            FileInputStream(overviewTablesFile).use {
                val workbook = XSSFWorkbook(it)

                overviewTablesOutput.listFiles().forEach { file ->
                    val csvData: CSVData = CSVReader(file.reader()).use { reader ->
                        val allCsvLines = reader.readAll()
                        val recentDate = allCsvLines.mapNotNull { row ->
                            Regex("\\d{4}-\\d{2}").find(row.find { cell -> cell.contains("recent refers to") }
                                .orEmpty())?.value
                        }.firstOrNull()
                        val dataRows = allCsvLines.dropWhile { strings -> strings[0].toIntOrNull() == null }
                            .associate { strings -> strings[0].toInt() to strings.toList() }.toSortedMap()

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

                        val dataRows = sheet.dropWhile { row -> row.first().numericCellValue != 1.0 }
                            .associateBy { row -> row.first().numericCellValue.toInt() }
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
    }

    class CSVData(
        val recentDate: String?,
        val dataRows: SortedMap<Int, List<String>>
    )

}