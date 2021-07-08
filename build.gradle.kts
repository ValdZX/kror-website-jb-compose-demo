import org.jetbrains.compose.compose
import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    kotlin("multiplatform") version "1.5.10"
    kotlin("plugin.serialization") version "1.5.10"
    id("org.jetbrains.compose") version "0.5.0-build235"
}

group = "ua.vald_zx"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
}

val ktorVersion by project.extra("1.6.1")
val logback_version by project.extra("1.2.3")

kotlin {
    jvm("jvmDesktop")
    jvm("jvmServer")
    js(IR) {
        useCommonJs()
        browser()
        binaries.executable()
    }

    val jsBrowserDistribution by tasks.getting

    tasks.getByName<ProcessResources>("jvmServerProcessResources") {
        dependsOn(jsBrowserDistribution)
        from(jsBrowserDistribution)
    }
    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(kotlin("stdlib-common"))
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.5.0")
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.2.1")
                implementation(compose.web.widgets)
                implementation(compose.runtime)
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test-common"))
                implementation(kotlin("test-annotations-common"))
            }
        }
        val jvmDesktopMain by getting {
            dependencies {
                implementation(compose.desktop.currentOs)
            }
        }
        val jvmServerMain by getting {
            dependencies {
                implementation("io.ktor:ktor-server-core:$ktorVersion")
                implementation("io.ktor:ktor-server-netty:$ktorVersion")
                implementation("io.ktor:ktor-html-builder:$ktorVersion")
                implementation("io.ktor:ktor-websockets:$ktorVersion")
                implementation("ch.qos.logback:logback-classic:$logback_version")
            }
        }
    }
}

compose.desktop {
    application {
        mainClass = "ua.vald_zx.ktor.compose.demo.desktop.AppKt"
        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "jvm"
            packageVersion = "1.0.0"
        }
    }
}

tasks.getByName<Jar>("jvmServerJar") {
    val taskName = if (project.hasProperty("isProduction")) {
        "jsBrowserProductionWebpack"
    } else {
        "jsBrowserDevelopmentWebpack"
    }
    val webpackTask = tasks.getByName<org.jetbrains.kotlin.gradle.targets.js.webpack.KotlinWebpack>(taskName)
    dependsOn(webpackTask)
    from(File(webpackTask.destinationDirectory, webpackTask.outputFileName))
}