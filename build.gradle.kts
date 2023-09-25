
fun isNonStable(version: String): Boolean {
    return listOf("alpha", "beta", "dev").any { version.lowercase().contains(it) }
}

plugins {
    kotlin("jvm")
    application
    id("com.github.ben-manes.versions")
    id("com.github.johnrengelman.shadow")
}

group = "de.stefanbissell.mekdb"
version = "1.0"

repositories {
    mavenCentral()
}

dependencies {

}

application {
    mainClass.set("de.stefanbissell.mekdb.MainKt")
}

tasks {
    dependencyUpdates {
        rejectVersionIf {
            isNonStable(candidate.version)
        }
    }
}

kotlin {
    jvmToolchain(11)
}
