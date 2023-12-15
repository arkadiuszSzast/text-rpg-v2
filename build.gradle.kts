import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.ktor)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.sonarqube)
    alias(libs.plugins.kover)
    alias(libs.plugins.spotless)
}

group = "com.szastarek"
version = "0.0.1"

java {
    sourceCompatibility = JavaVersion.VERSION_21
}

kotlin {
    jvmToolchain(21)
}

dependencies {
    kover(project(":application:main"))
    kover(project(":application:shared"))
    kover(project(":application:monitoring"))
    kover(project(":application:security"))
    kover(project(":application:documentation"))
    kover(project(":application:mediator"))
    kover(project(":application:acl"))
    kover(project(":application:mail"))
    kover(project(":application:event-store"))
    kover(project(":application:redis"))
    kover(project(":application:test-utils"))
    kover(project(":application:account"))
    kover(project(":application:world"))
}

sonar {
    properties {
        property("sonar.projectKey", "arkadiuszSzast_text-rpg-v2")
        property("sonar.organization", "arkadiuszszast")
        property("sonar.host.url", "https://sonarcloud.io")
        property("sonar.coverage.jacoco.xmlReportPaths", "${project.buildDir}/reports/kover/report.xml")
        property("sonar.coverage.exclusions", "**/Application.kt,**/plugin/**")
    }
}

spotless {
    kotlin {
        target("/application/**/*.kt")
        ktlint()
    }
}

application {
    mainClass.set("io.ktor.server.netty.EngineMain")

    val isDevelopment: Boolean = project.ext.has("development")
    applicationDefaultJvmArgs = listOf("-Dio.ktor.development=$isDevelopment")
}

allprojects {
    repositories {
        mavenCentral()
    }

    apply(plugin = "kotlin")
    apply(plugin = "org.jetbrains.kotlinx.kover")
}

subprojects {
    apply(plugin = "org.jetbrains.kotlin.plugin.serialization")
    apply(plugin = "org.gradle.java-test-fixtures")

    dependencies {
        implementation(rootProject.libs.ktor.server.core.jvm)
        implementation(rootProject.libs.ktor.server.auth.jvm)
        implementation(rootProject.libs.ktor.server.auth.jwt.jvm)
        implementation(rootProject.libs.ktor.server.host.common.jvm)
        implementation(rootProject.libs.ktor.server.cors.jvm)
        implementation(rootProject.libs.ktor.server.content.negotiation.jvm)
        implementation(rootProject.libs.ktor.serialization.kotlinx.json.jvm)
        implementation(rootProject.libs.ktor.server.netty.jvm)

        implementation(rootProject.libs.logback.classic)
        implementation(rootProject.libs.arrow.core)
        implementation(rootProject.libs.arrow.coroutines)
        implementation(rootProject.libs.jbcrypt)
        implementation(rootProject.libs.kotlinx.datetime)
        implementation(rootProject.libs.opentelemetry.logback)
        implementation(rootProject.libs.koin.ktor)
        implementation(rootProject.libs.kmongo.id.serialization)
        implementation(rootProject.libs.kotlin.logging.jvm)


        testImplementation(rootProject.libs.koin.test)
        testImplementation(rootProject.libs.kotest.extensions.koin)
        testImplementation(rootProject.libs.kotest.property.jvm)
        testImplementation(rootProject.libs.ktor.client.content.negotiation)

        testImplementation(rootProject.libs.ktor.server.tests.jvm)
        testImplementation(rootProject.libs.kotlin.test.junit)
        testImplementation(rootProject.libs.kotest.runner.junit5)
        testImplementation(rootProject.libs.kotest.assertions.core)
        testImplementation(rootProject.libs.kotest.assertions.arrow)
        testImplementation(rootProject.libs.awaitility)
        testImplementation(rootProject.libs.faker)
    }

    tasks.withType<Test>().configureEach {
        useJUnitPlatform()
        jvmArgs("--add-opens=java.base/java.util=ALL-UNNAMED")
    }

    kotlin.target.compilations["testFixtures"].associateWith(kotlin.target.compilations["main"])
}
