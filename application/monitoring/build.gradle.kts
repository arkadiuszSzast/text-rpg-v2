val ktor_version: String by project
val opentelemetry_version: String by project
val opentelemetry_ktor_version: String by project
val micrometer_otlp_registry_version: String by project

dependencies {
    implementation(project(":application:shared"))

    api("io.opentelemetry.instrumentation:opentelemetry-ktor-2.0:$opentelemetry_ktor_version")
    api("io.opentelemetry:opentelemetry-extension-kotlin:$opentelemetry_version")
    implementation("io.ktor:ktor-server-metrics-micrometer:$ktor_version")
    implementation("io.micrometer:micrometer-registry-otlp:$micrometer_otlp_registry_version")
    implementation("io.ktor:ktor-server-call-logging-jvm:$ktor_version")

    testImplementation("io.opentelemetry:opentelemetry-sdk-testing:$opentelemetry_version")
}
