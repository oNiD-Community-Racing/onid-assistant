import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinVersion
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    application

    alias(libs.plugins.kotlin)
    alias(libs.plugins.shadow)
    alias(libs.plugins.detekt)
    alias(libs.plugins.git.hooks)
}

group = "io.github.nocomment1105.onidassistant"
version = "0.1.0"

val javaVersion = 17

repositories {
    mavenCentral()

    maven {
        name = "Sonatype Snapshots (Legacy)"
        url = uri("https://oss.sonatype.org/content/repositories/snapshots")
    }

    maven {
        name = "Sonatype Snapshots"
        url = uri("https://s01.oss.sonatype.org/content/repositories/snapshots/")
    }
}

dependencies {
    detektPlugins(libs.detekt)

    // Kord Extensions
    implementation(libs.kord.extensions.core)
    implementation(libs.kord.extensions.unsafe)

    implementation(libs.kotlin.stdlib)

    // Logging Deps
    implementation(libs.logging)
    implementation(libs.logback)
}

application {
    mainClass.set("io.github.nocomment1105.onidassistant.OnidAssistantKt")
}

gitHooks {
    setHooks(
        mapOf("pre-commit" to "detekt")
    )
}

tasks {
    withType<KotlinCompile> {
        compilerOptions {
            jvmTarget.set(JvmTarget.fromTarget(javaVersion.toString()))
            languageVersion.set(KotlinVersion.fromVersion(libs.plugins.kotlin.get().version.requiredVersion.substringBeforeLast(".")))
            incremental = true
            freeCompilerArgs.add("-opt-in=kotlin.RequiresOptIn")
        }
    }

    jar {
        manifest {
            attributes("Main-Class" to "io.github.nocomment1105.onidassistant.OnidAssistant")
        }
    }

    wrapper {
        distributionType = Wrapper.DistributionType.BIN
    }
}

kotlin {
    jvmToolchain(javaVersion)
}

detekt {
    buildUponDefaultConfig = true
    config.setFrom("$rootDir/detekt.yml")

    autoCorrect = true
}