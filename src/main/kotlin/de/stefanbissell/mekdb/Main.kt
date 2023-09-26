package de.stefanbissell.mekdb

import megamek.common.Mech
import megamek.common.loaders.MtfFile
import megameklab.printing.PaperSize
import megameklab.printing.PrintMech
import megameklab.printing.RecordSheetOptions
import java.awt.print.PageFormat
import java.io.*
import java.net.URL
import java.util.*
import java.util.zip.ZipInputStream

fun main() {
    Locale.setDefault(Locale.US)

    val dataDir = File("data")
    dataDir.mkdirs()
    val mekLabJar = File(dataDir, "megameklab-0.49.14.zip")

    downloadData(mekLabJar)

    File("files/style.css").copyTo(File("webpage/style.css"), overwrite = true)
    File("files/main.js").copyTo(File("webpage/main.js"), overwrite = true)

    val mechs = ZipInputStream(FileInputStream(mekLabJar)).use { zip ->
        generateSequence { zip.nextEntry }
            .filter { !it.isDirectory }
            .filter { it.name.endsWith("mtf") }
            .map {
                val bytes = zip.readAllBytes()
                val path = it.name
                zip.closeEntry()
                readMechFile(path, ByteArrayInputStream(bytes))
            }
            .toList()
            .sortedBy { it.mech.chassis }
    }
    createIndexFile(mechs)
    mechs.forEach {
        println("${it.path}/${it.filename}.mtf")
        storeRecordSheet(it)
    }
    println(mechs.count())
}

private fun downloadData(mekLabJar: File) {
    if (!mekLabJar.exists()) {
        val mekLabJarUrl = URL("https://github.com/MegaMek/megameklab/archive/refs/tags/v0.49.14.zip")
        println("Downloading $mekLabJarUrl")
        mekLabJarUrl.openStream().use {
            it.copyTo(FileOutputStream(mekLabJar))
        }
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
