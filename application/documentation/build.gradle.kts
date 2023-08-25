val ktor_version: String by project
val opentelemetry_version: String by project
val opentelemetry_ktor_version: String by project
val micrometer_otlp_registry_version: String by project

dependencies {
    implementation(project(":application:shared"))

    implementation("io.ktor:ktor-server-swagger-jvm:$ktor_version")
}
