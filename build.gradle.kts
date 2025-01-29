@file:Suppress("UnstableApiUsage")

plugins {
    idea
    java
    id("cc.polyfrost.loom") version "0.10.0.+"
    id("dev.architectury.architectury-pack200") version "0.1.3"
    id("com.github.johnrengelman.shadow") version "8.1.1"
//    id("io.freefair.lombok") version "8.6"
    id("net.kyori.blossom") version "1.3.2"
}

//Constants:

val baseGroup: String by project
val mcVersion: String by project
val version: String by project
val mixinGroup = "$baseGroup.mixin"
val modid: String by project
val modName: String by project

// Toolchains:
java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(8))
}

blossom {
    replaceToken("%%VERSION%%", version)
}

// Minecraft configuration:
loom {
    launchConfigs {
        "client" {
            // If you don't want mixins, remove these lines
            property("mixin.debug", "true")
            property("asmhelper.verbose", "true")
            arg("--tweakClass", "cc.polyfrost.oneconfig.loader.stage0.LaunchWrapperTweaker")
            arg("-Dfml.coreMods.load", "com.jelly.farmhelperv2.transformer.FMLCore")
            arg("--tweakClass", "com.jelly.farmhelperv2.transformer.Tweaker")
        }
    }
    forge {
        pack200Provider.set(dev.architectury.pack200.java.Pack200Adapter())
        // If you don't want mixins, remove this lines
        mixinConfig("mixins.$modid.json", "mixins.baritone.json")
    }
    // If you don't want mixins, remove these lines
    mixin {
        defaultRefmapName.set("mixins.$modid.refmap.json")
    }
}

sourceSets.main {
    output.setResourcesDir(sourceSets.main.flatMap { it.java.classesDirectory })
}

// Dependencies:

repositories {
    mavenCentral()
    maven("https://repo.spongepowered.org/maven/")
    maven("https://pkgs.dev.azure.com/djtheredstoner/DevAuth/_packaging/public/maven/v1")
    maven("https://repo.polyfrost.cc/releases")
    maven("https://repo.essential.gg/repository/maven-public")
    maven("https://jitpack.io")
}

val shadowImpl: Configuration by configurations.creating {
    configurations.implementation.get().extendsFrom(this)
}

dependencies {
    minecraft("com.mojang:minecraft:1.8.9")
    mappings("de.oceanlabs.mcp:mcp_stable:22-1.8.9")
    forge("net.minecraftforge:forge:1.8.9-11.15.1.2318-1.8.9")

    compileOnly("cc.polyfrost:oneconfig-1.8.9-forge:0.2.1-alpha+")
    shadowImpl("cc.polyfrost:oneconfig-wrapper-launchwrapper:1.0.0-beta+")

    compileOnly("org.spongepowered:mixin:0.8.5")
    annotationProcessor("org.spongepowered:mixin:0.8.5")

    compileOnly("org.projectlombok:lombok:1.18.34")
    annotationProcessor("org.projectlombok:lombok:1.18.34")

    shadowImpl("org.java-websocket:Java-WebSocket:1.5.4")

    implementation("me.djtheredstoner:DevAuth-forge-legacy:1.2.1")

    implementation("net.dv8tion:JDA:5.1.2")

    implementation("com.github.onixiya1337.baritone-fly:baritone-deobf:nirox-fly-SNAPSHOT")
    shadowImpl("com.github.onixiya1337.baritone-fly:baritone-api-forge:nirox-fly-SNAPSHOT") {
        exclude(module = "fastutil")
        exclude(module = "lwjgl")
        exclude(module = "SimpleTweaker")
        exclude(module = "launchwrapper")
    }
}

// Tasks:

tasks.withType(JavaCompile::class) {
    options.encoding = "UTF-8"
}

tasks.withType(Jar::class) {
    archiveBaseName.set(modName)
    manifest.attributes.run {
        this["FMLCorePlugin"] = "com.jelly.farmhelperv2.transformer.FMLCore"
        this["FMLCorePluginContainsFMLMod"] = "true"
        this["ForceLoadAsMod"] = "true"
        this["TweakOrder"] = "0"
        this["ModSide"] = "CLIENT"

        // If you don't want mixins, remove these lines
        this["TweakClass"] = "cc.polyfrost.oneconfig.loader.stage0.LaunchWrapperTweaker"
        this["MixinConfigs"] = "mixins.$modid.json, mixins.baritone.json"
    }
}

tasks.processResources {
    inputs.property("version", project.version)
    inputs.property("mcversion", mcVersion)
    inputs.property("modid", modid)
    inputs.property("modName", modName)
    inputs.property("mixinGroup", mixinGroup)

    filesMatching(listOf("mcmod.info", "mixins.$modid.json")) {
        expand(inputs.properties)
    }

    rename("(.+_at.cfg)", "META-INF/$1")
}


val remapJar by tasks.named<net.fabricmc.loom.task.RemapJarTask>("remapJar") {
    archiveClassifier.set("")
    from(tasks.shadowJar)
    input.set(tasks.shadowJar.get().archiveFile)
}

tasks.jar {
    archiveClassifier.set("without-deps")
    destinationDirectory.set(layout.buildDirectory.dir("badjars"))
}

tasks.shadowJar {
    destinationDirectory.set(layout.buildDirectory.dir("badjars"))
    archiveClassifier.set("all-dev")
    configurations = listOf(shadowImpl)
    doLast {
        configurations.forEach {
            println("Copying jars into mod: ${it.files}")
        }
    }

    // If you want to include other dependencies and shadow them, you can relocate them in here
    fun relocate(name: String) = relocate(name, "$baseGroup.deps.$name")
}

tasks.assemble.get().dependsOn(tasks.remapJar)

