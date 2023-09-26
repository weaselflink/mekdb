package de.stefanbissell.mekdb

import kotlinx.html.*
import kotlinx.html.dom.createHTMLDocument
import kotlinx.html.dom.write
import megamek.common.Mech
import megamek.common.Mounted
import megamek.common.SimpleTechLevel
import java.io.File
import java.io.FileWriter

fun createIndexFile(mechs: List<MechEntry>) {
    val file = File("webpage/index.html")
    val fileWriter = FileWriter(file)

    val content = createHTMLDocument().html {
        head {
            link(rel = "stylesheet", href = "style.css")
        }
        body {
            div(classes = "filters") {
                input(classes = "search", type = InputType.text) {
                    attributes["placeholder"] = "Search..."
                }
                span { +"Tech" }
                select(classes = "tech") {
                    option { +"All" }
                    option { +"IS" }
                    option { +"Clan" }
                }
                span { +"Rules" }
                select(classes = "level") {
                    option { +"All" }
                    option { +"Int" }
                    option { +"Std" }
                    option { +"Adv" }
                    option { +"Exp" }
                    option { +"Un" }
                }
            }
            table {
                thead {
                    tr {
                        th { +"Name" }
                        th { +"Model" }
                        th { +"Tech" }
                        th { +"Level" }
                        th { +"Mass" }
                        th { +"Speed" }
                        th { +"Armor" }
                        th { +"Weapons" }
                        th { +"Year" }
                        th { +"Sheet" }
                    }
                }
                tbody {
                    mechs.forEach {
                        val mech = it.mech
                        tr {
                            td(classes = "chassis") { +mech.chassis }
                            td(classes = "model") { +mech.model }
                            td(classes = "tech") { +mech.techBaseSummary() }
                            td(classes = "level") { +mech.rulesLevelSummary() }
                            td(classes = "mass") { +mech.weight.toInt().toString() }
                            td(classes = "speed") { +mech.speedSummary() }
                            td(classes = "armor") { +mech.totalArmor.toString() }
                            td(classes = "weapons") { +mech.weaponSummary() }
                            td(classes = "weapons") { +mech.year.toString() }
                            td {
                                a(
                                    href = "mechs/${it.path}/${it.filename}.pdf",
                                    target = "_blank"
                                ) { +"PDF" }
                            }
                        }
                    }
                }
            }
            script(type = "application/ecmascript", src = "main.js") {}
        }
    }

    fileWriter.write(content, prettyPrint = true)
    fileWriter.close()
}

private fun Mech.techBaseSummary(): String {
    return if (isMixedTech) {
        if (isClan) {
            "Mixed Clan"
        } else {
            "Mixed IS"
        }
    } else {
        if (isClan) {
            "Clan"
        } else {
            "IS"
        }
    }
}

private fun Mech.rulesLevelSummary(): String {
    return when (staticTechLevel) {
        SimpleTechLevel.INTRO -> "Int"
        SimpleTechLevel.STANDARD -> "Std"
        SimpleTechLevel.ADVANCED -> "Adv"
        SimpleTechLevel.EXPERIMENTAL -> "Exp"
        SimpleTechLevel.UNOFFICIAL -> "Un"
        else -> "-"
    }
}

private fun Mech.speedSummary(): String {
    return "$walkMP/$runMP/$jumpMP"
}

private fun Mech.weaponSummary(): String {
    return weaponList
        .groupBy { it.shortName }
        .map { (_, list) -> WeaponEntry(list.first(), list.size) }
        .sortedBy { it.mounted.tonnage }
        .reversed()
        .joinToString(separator = ", ") {
            val name = it.mounted.shortName
            if (it.count > 1) "$name x ${it.count}" else name
        }
}

private data class WeaponEntry(
    val mounted: Mounted,
    val count: Int
)
