dependencies {
    implementation(project(":application:shared"))

    api(libs.opentelemetry.ktor)
    api(libs.opentelemetry.kotin)
    implementation(libs.ktor.server.metrics.micrometer)
    implementation(libs.micrometer.registry.otlp)
    implementation(libs.ktor.server.call.logging.jvm)

    testImplementation(testFixtures(project(":application:test-utils")))
}
