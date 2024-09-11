import org.gradle.kotlin.dsl.libs
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import xyz.jpenilla.runpaper.task.RunServer

plugins {
    kotlin("jvm") version "2.0.20"
    alias(libs.plugins.indra) apply false
    alias(libs.plugins.indraGit)
    alias(libs.plugins.runPaper)
    alias(libs.plugins.shadow)
    alias(libs.plugins.indraSpotless) apply false
    alias(libs.plugins.spotless)
}

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/") {
        name = "papermc-repo"
    }
    maven("https://oss.sonatype.org/content/groups/public/") {
        name = "sonatype"
    }
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:1.21.1-R0.1-SNAPSHOT")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")

    implementation("net.kyori", "adventure-extra-kotlin")
    implementation("net.kyori", "adventure-serializer-configurate4")

    implementation("org.spongepowered", "configurate-hocon")
    implementation("org.spongepowered", "configurate-extra-kotlin")

    implementation("io.insert-koin", "koin-core", "3.5.6")
    implementation("org.bstats", "bstats-bukkit", "3.0.3")
    implementation("io.papermc", "paperlib", "1.0.8")
}

version = (version as String)

kotlin {
  jvmToolchain {
    languageVersion = JavaLanguageVersion.of(21)
  }
  compilerOptions {
    jvmTarget = JvmTarget.JVM_17
    freeCompilerArgs = listOf("-Xjdk-release=17")
  }
}

java.disableAutoTargetJvm()

tasks {
    build {
        dependsOn(shadowJar)
    }
    runServer {
        minecraftVersion("1.21.1")
    }

  register("format") {
    group = "formatting"
    description = "Formats source code according to project style."
    dependsOn(spotlessApply)
  }

  processResources {
    val props = mapOf(
      "version" to project.version,
      "website" to "https://github.com/imide/PaperBackups",
      "description" to project.description,
      "apiVersion" to "1.13",
    )
    inputs.properties(props)
    filteringCharset = "UTF-8"
    filesMatching("plugin.yml") {
      expand(props)
    }
  }
}


spotless {
  val overrides = mapOf(
    "ktlint_standard_filename" to "disabled",
    "ktlint_standard_trailing-comma-on-call-site" to "disabled",
    "ktlint_standard_trailing-comma-on-declaration-site" to "disabled",
  )
  kotlin {
    ktlint(libs.versions.ktlint.get()).editorConfigOverride(overrides)
  }
  kotlinGradle {
    ktlint(libs.versions.ktlint.get()).editorConfigOverride(overrides)
  }
}

// The following is to work around https://github.com/diffplug/spotless/issues/1599
// Ensure the ktlint step is before the license header step
plugins.apply(libs.plugins.indraSpotless.get().pluginId)
