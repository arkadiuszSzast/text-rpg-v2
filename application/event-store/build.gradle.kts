val event_store_version: String by project
val test_containers_version: String by project
val opentelemetry_version: String by project

dependencies {
    implementation(project(":application:shared"))
    implementation(project(":application:monitoring"))

    implementation("com.eventstore:db-client-java:$event_store_version")

    testImplementation("org.testcontainers:testcontainers:$test_containers_version")
    testImplementation("io.opentelemetry:opentelemetry-sdk-testing:$opentelemetry_version")
}
