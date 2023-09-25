package de.stefanbissell.mekdb

import kotlinx.html.*
import kotlinx.html.dom.createHTMLDocument
import kotlinx.html.dom.write
import megamek.common.Mech
import megamek.common.loaders.MtfFile
import megameklab.printing.PaperSize
import megameklab.printing.PrintMech
import megameklab.printing.RecordSheetOptions
import java.awt.print.PageFormat
import java.io.*
import java.util.*
import java.util.zip.ZipInputStream

fun main() {
    Locale.setDefault(Locale.US)
    val mechs = ZipInputStream(FileInputStream("data/megamek-0.49.14.zip")).use { zip ->
        generateSequence { zip.nextEntry }
            .filter { !it.isDirectory }
            .filter { it.name.endsWith("mtf") }
            .map {
                val bytes = zip.readAllBytes()
                val path = it.name
                zip.closeEntry()
                readMechFile(path, ByteArrayInputStream(bytes))
            }
            .filter { it.mech.isIntroLevel }
            .filter { it.path == "3039u" }
            .toList()
            .subList(0, 50)
    }
    createIndexFile(mechs)
    mechs.forEach {
        println("${it.mech.model} --- ${it.mech.chassis}")
        storeRecordSheet(it)
    }
}

private fun createIndexFile(mechs: List<MechEntry>) {
    val file = File("webpage/index.html")
    val fileWriter = FileWriter(file)

    val content = createHTMLDocument().html {
        body {
            table {
                thead {
                    tr {
                        th { +"Model" }
                        th { +"Name" }
                        th { +"Tonnage" }
                        th { +"Sheet" }
                    }
                }
                tbody {
                    mechs.forEach {
                        val mech = it.mech
                        tr {
                            td { +mech.model }
                            td { +mech.chassis }
                            td { +mech.weight.toInt().toString() }
                            td { a(href = "mechs/${it.path}/${it.filename.removeSuffix(".mtf")}.pdf") { +"PDF" } }
                        }
                    }
                }
            }
        }
    }

    fileWriter.write(content, prettyPrint = true)
    fileWriter.close()
}

private fun readMechFile(path: String, stream: InputStream): MechEntry {
    val mech = MtfFile(stream).entity as Mech
    val shortPath = path.substringAfterLast("mechfiles/mechs/")
    return MechEntry(
        path = shortPath.substringBeforeLast("/"),
        filename = shortPath.substringAfterLast("/"),
        mech = mech
    )
}

private fun storeRecordSheet(entry: MechEntry) {
    val options = RecordSheetOptions()
        .also {
            it.paperSize = PaperSize.ISO_A4
            it.setColor(false)
        }

    val pdf = PrintMech(
        entry.mech,
        0,
        options
    )
    val stream = pdf.exportPDF(0, PageFormat().also { it.paper = options.paperSize.createPaper() })

    val file = File("webpage/mechs/${entry.path}/${entry.filename.removeSuffix(".mtf")}.pdf")
    file.parentFile.mkdirs()
    val out = FileOutputStream(file)

    out.use {
        stream.use {
            it.copyTo(out)
        }
    }
}

data class MechEntry(
    val path: String,
    val filename: String,
    val mech: Mech
)
