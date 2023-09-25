package de.stefanbissell.mekdb

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

    File("files/style.css").copyTo(File("webpage/style.css"), overwrite = true)
    File("files/main.js").copyTo(File("webpage/main.js"), overwrite = true)

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
            .filter { it.path in listOf("3039u", "3050U", "3055U", "3058Uu") }
            .toList()
            .sortedBy { it.mech.model }
    }
    createIndexFile(mechs)
    mechs.forEach {
        println("${it.path}/${it.filename}.mtf")
        storeRecordSheet(it)
    }
}

private fun readMechFile(path: String, stream: InputStream): MechEntry {
    val mech = MtfFile(stream).entity as Mech
    val shortPath = path.substringAfterLast("mechfiles/mechs/")
    return MechEntry(
        path = shortPath.substringBeforeLast("/"),
        filename = shortPath.substringAfterLast("/").removeSuffix(".mtf"),
        mech = mech
    )
}

private fun storeRecordSheet(entry: MechEntry) {
    val file = File("webpage/mechs/${entry.path}/${entry.filename}.pdf")
    if (file.exists()) {
        return
    }
    file.parentFile.mkdirs()

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
