val ktor_version: String by project
val kotlin_version: String by project
val logback_version: String by project
val opentelemetry_logback_version: String by project
val kotlin_logging_version: String by project
val kotest_version: String by project
val koin_version: String by project
val kotest_koin_version: String by project
val kmongo_version: String by project
val awaitility_version: String by project
val arrow_version: String by project
val jbcrypt_version: String by project
val kotest_arrow_version: String by project
val kotlin_datetime_version: String by project
val faker_version: String by project
val redisson_version: String by project

plugins {
    kotlin("jvm") version "1.9.21"
    id("io.ktor.plugin") version "2.3.3"
    id("org.jetbrains.kotlin.plugin.serialization") version "1.9.10"
    id("org.sonarqube") version "4.2.1.3168"
    id("org.jetbrains.kotlinx.kover") version "0.7.5"
}

group = "com.szastarek"
version = "0.0.1"
java.sourceCompatibility = JavaVersion.VERSION_17

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
        implementation("io.ktor:ktor-server-core-jvm:$ktor_version")
        implementation("io.ktor:ktor-server-auth-jvm:$ktor_version")
        implementation("io.ktor:ktor-server-auth-jwt-jvm:$ktor_version")
        implementation("io.ktor:ktor-server-host-common-jvm:$ktor_version")
        implementation("io.ktor:ktor-server-cors-jvm:$ktor_version")
        implementation("io.ktor:ktor-server-content-negotiation-jvm:$ktor_version")
        implementation("io.ktor:ktor-serialization-kotlinx-json-jvm:$ktor_version")
        implementation("io.ktor:ktor-server-netty-jvm:$ktor_version")
        implementation("ch.qos.logback:logback-classic:$logback_version")
        implementation("io.arrow-kt:arrow-core:$arrow_version")
        implementation("io.arrow-kt:arrow-fx-coroutines:$arrow_version")
        implementation("org.mindrot:jbcrypt:$jbcrypt_version")
        implementation("org.jetbrains.kotlinx:kotlinx-datetime:$kotlin_datetime_version")

        implementation("io.opentelemetry.instrumentation:opentelemetry-logback-1.0:$opentelemetry_logback_version")
        implementation("io.insert-koin:koin-ktor:$koin_version")
        implementation("org.litote.kmongo:kmongo-id-serialization:$kmongo_version")
        implementation("io.github.oshai:kotlin-logging-jvm:$kotlin_logging_version")

        testImplementation("io.insert-koin:koin-test:$koin_version")
        testImplementation("io.kotest.extensions:kotest-extensions-koin:${kotest_koin_version}")
        testImplementation("io.kotest:kotest-property-jvm:$kotest_version")
        testImplementation("io.ktor:ktor-client-content-negotiation:$ktor_version")

        testImplementation("io.ktor:ktor-server-tests-jvm:$ktor_version")
        testImplementation("org.jetbrains.kotlin:kotlin-test-junit:$kotlin_version")
        testImplementation("io.kotest:kotest-runner-junit5:$kotest_version")
        testImplementation("io.kotest:kotest-assertions-core:$kotest_version")
        testImplementation("io.kotest.extensions:kotest-assertions-arrow:$kotest_arrow_version")
        testImplementation("org.awaitility:awaitility-kotlin:$awaitility_version")
        testImplementation("io.github.serpro69:kotlin-faker:$faker_version")
    }

    tasks.withType<Test>().configureEach {
        useJUnitPlatform()
        jvmArgs("--add-opens=java.base/java.util=ALL-UNNAMED")
    }

    kotlin.target.compilations["testFixtures"].associateWith(kotlin.target.compilations["main"])
}
