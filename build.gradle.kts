
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
    maven {
        url = uri("https://github.com/MegaMek/mavenrepo/raw/master")
    }
    mavenCentral()
    maven {
        url = uri("https://jitpack.io")
    }
}

dependencies {
    implementation("org.megamek:megamek:0.49.15-SNAPSHOT")
    implementation("org.megamek:megameklab:0.49.15-SNAPSHOT")
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
