import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

val modId: String by project
val minecraftVersion: String by project
val forgeVersion: String by project
val modVersion: String by project
val shadowLibrary: Configuration by configurations.creating {
    isCanBeResolved = true
    isCanBeConsumed = false
}

plugins {
    java
    idea
    id("dev.architectury.loom") version "1.6-SNAPSHOT"
    kotlin("jvm")
    kotlin("plugin.serialization")
    id("com.github.johnrengelman.shadow") version "8.1.1"
}

java.withSourcesJar()

configurations {
    compileClasspath { extendsFrom(shadowLibrary) }
    runtimeClasspath { extendsFrom(shadowLibrary) }

}

loom {
    silentMojangMappingsLicense()

    val awFile = project.file("src/main/resources/$modId.accesswidener")
    if (awFile.exists()) accessWidenerPath = awFile

    forge {
        convertAccessWideners = true
        extraAccessWideners.add(loom.accessWidenerPath.get().asFile.name)
        mixinConfigs("$modId.mixins.json")
    }
}

version = modVersion

base {
    archivesName = project.properties["archivesName"].toString()
}

repositories {
    mavenCentral()
    maven("https://maven.0mods.team/releases")
    maven("https://maven.minecraftforge.net/")
    maven("https://maven.architectury.dev/")
    maven("https://maven.fabricmc.net/")
    maven("https://maven.parchmentmc.org")
    maven("https://thedarkcolour.github.io/KotlinForForge/")
    maven("https://maven.blamejared.com/")
    maven("https://modmaven.dev")
    maven("https://maven.tterrag.com/")
    maven("https://maven.0mods.team/releases")
    maven("https://repo.spongepowered.org/repository/maven-public/")
}

dependencies {
    minecraft("com.mojang:minecraft:${minecraftVersion}")
    mappings(loom.layered {
        officialMojangMappings()
        parchment("org.parchmentmc.data:parchment-${minecraftVersion}:${project.properties["parchmentVersion"].toString()}@zip")
    })

    compileOnly("org.spongepowered:mixin:0.8")

    forge("net.minecraftforge:forge:${minecraftVersion}-${forgeVersion}")

    fun shadow(dep: Any) {
        include(dep)
        minecraftClientRuntimeLibraries(dep)
    }

    shadowLibrary("team.0mods:KotlinExtras:1.4-noreflect")

    shadow("team.chisel.ctm:CTM:${minecraftVersion}-${project.properties["ctm_version"].toString()}")

    implementation(kotlin("stdlib", "2.0.10"))
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:+")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core-jvm:+")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-core:+")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:+")

    modApi("mezz.jei:jei-${minecraftVersion}-forge:${project.properties["jei_version"].toString()}")
}

tasks {
    jar {
        manifest {
            attributes(
                mapOf(
                    "Specification-Title" to project.properties["modName"].toString(),
                    "Specification-Vendor" to "0mods",
                    "Specification-Version" to "1",
                    "Implementation-Title" to project.name,
                    "Implementation-Version" to version,
                    "Implementation-Timestamp" to ZonedDateTime.now()
                        .format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssZ")),
                    "MixinConfigs" to "$modId.mixins.json"
                )
            )
        }
    }

    shadowJar {
        configurations = listOf(shadowLibrary)
        archiveClassifier = "dev-shadow"

        val relocateLibs = listOf(
            "org.jetbrains", "com.typesafe", "kotlinx",
            "kotlin", "okio", "org.intellij", "_COROUTINE"
        )

        relocateLibs.forEach {
            relocate(it, "ecr_libs.$it")
        }
    }

    compileKotlin {
        useDaemonFallbackStrategy = false
        compilerOptions.freeCompilerArgs.add("-Xjvm-default=all")
    }

    processResources {
        from(project.sourceSets.main.get().resources)

        val replacement = mapOf(
            "modId" to modId, "modVersion" to modVersion, "modName" to project.properties["modName"].toString(),
            "modCredits" to project.properties["modCredits"].toString(), "modAuthors" to project.properties["modAuthors"].toString(),
            "modDesc" to project.properties["modDesc"].toString(), "forgeVersionRange" to project.properties["forgeVersionRange"].toString(),
            "minecraftVersionRange" to project.properties["minecraftVersionRange"].toString(), "loaderVersionRange" to project.properties["loaderVersionRange"].toString(),
            "modLicense" to project.properties["modLicense"]
        )

        filesMatching(listOf("META-INF/mods.toml", "pack.mcmeta", "*.mixins.json")) {
            expand(replacement)
        }

        inputs.properties(replacement)
    }

    remapJar {
        inputFile = shadowJar.get().archiveFile
    }

    withType<JavaCompile> {
        options.encoding = "UTF-8"
        options.release = 17
    }
}

kotlin {
    jvmToolchain(17)
}
