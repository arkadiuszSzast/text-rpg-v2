val ktor_version: String by project
val kotlin_version: String by project
val logback_version: String by project
val opentelemetry_logback_version: String by project
val kotlin_logging_version: String by project
val kotest_version: String by project
val koin_version: String by project
val kotest_koin_version: String by project
val kmongo_version: String by project

plugins {
    kotlin("jvm") version "1.9.10"
    id("io.ktor.plugin") version "2.3.3"
    id("org.jetbrains.kotlin.plugin.serialization") version "1.9.10"
    id("org.sonarqube") version "4.2.1.3168"
    id("jacoco")
}

group = "com.szastarek"
version = "0.0.1"

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
}

kotlin {
    jvmToolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
}

sonar {
    properties {
        property("sonar.projectKey", "arkadiuszSzast_text-rpg-v2")
        property("sonar.organization", "arkadiuszszast")
        property("sonar.host.url", "https://sonarcloud.io")
    }
}

application {
    mainClass.set("io.ktor.server.netty.EngineMain")

    val isDevelopment: Boolean = project.ext.has("development")
    applicationDefaultJvmArgs = listOf("-Dio.ktor.development=$isDevelopment")
}

allprojects {
    group = "com.szastarek"
    version = "0.0.1"

    repositories {
        mavenCentral()
    }

    apply(plugin = "kotlin")
    apply(plugin = "jacoco")
}

subprojects {
    apply(plugin = "org.jetbrains.kotlin.plugin.serialization")
    apply(plugin = "org.gradle.java-test-fixtures")

    dependencies {
        implementation("io.ktor:ktor-server-core-jvm:$ktor_version")
        implementation("io.ktor:ktor-server-auth-jvm:$ktor_version")
        implementation("io.ktor:ktor-server-auth-jwt-jvm:$ktor_version")
        implementation("io.ktor:ktor-server-host-common-jvm:$ktor_version")
        implementation("io.ktor:ktor-server-cors-jvm:$ktor_version")
        implementation("io.ktor:ktor-server-content-negotiation-jvm:$ktor_version")
        implementation("io.ktor:ktor-serialization-kotlinx-json-jvm:$ktor_version")
        implementation("io.ktor:ktor-server-netty-jvm:$ktor_version")
        implementation("ch.qos.logback:logback-classic:$logback_version")

        implementation("io.opentelemetry.instrumentation:opentelemetry-logback-1.0:$opentelemetry_logback_version")
        implementation("io.insert-koin:koin-ktor:$koin_version")
        implementation("org.litote.kmongo:kmongo-id-serialization:$kmongo_version")
        implementation("io.github.oshai:kotlin-logging-jvm:$kotlin_logging_version")

        testImplementation("io.insert-koin:koin-test:$koin_version")
        testImplementation("io.kotest.extensions:kotest-extensions-koin:${kotest_koin_version}")

        testImplementation("io.ktor:ktor-server-tests-jvm:$ktor_version")
        testImplementation("org.jetbrains.kotlin:kotlin-test-junit:$kotlin_version")
        testImplementation("io.kotest:kotest-runner-junit5:$kotest_version")
        testImplementation("io.kotest:kotest-assertions-core:$kotest_version")
    }

    tasks.withType<Test>().configureEach {
        useJUnitPlatform()
    }

    tasks.jacocoTestReport {
        reports {
            xml.required.set(true)
            csv.required.set(true)
            html.required.set(true)
        }
    }
}

task<JacocoReport>("jacocoRootReport") {
    dependsOn(subprojects.map { it.tasks.withType<Test>() })
    dependsOn(subprojects.map { it.tasks.withType<JacocoReport>() })
    additionalSourceDirs.setFrom(subprojects.map { it.the<SourceSetContainer>()["main"].allSource.srcDirs })
    sourceDirectories.setFrom(subprojects.map { it.the<SourceSetContainer>()["main"].allSource.srcDirs })
    classDirectories.setFrom(subprojects.map { it.the<SourceSetContainer>()["main"].output })
    classDirectories.setFrom(
        files(subprojects.flatMap { it.the<SourceSetContainer>()["main"].output }.map {
            fileTree(it) {
                exclude("**/Application.kt")
            }
        })
    )
    executionData.setFrom(
        project.fileTree(".") {
            include("**/build/jacoco/test.exec")
        }
    )
    reports {
        xml.required.set(true)
        csv.required.set(true)
        html.required.set(true)
        html.outputLocation.set(file("$buildDir/reports/jacoco/html"))
    }
}
