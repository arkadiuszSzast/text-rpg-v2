val event_store_version: String by project
val test_containers_version: String by project
val grpc_api: String by project
val koin_version: String by project

dependencies {
    implementation(project(":application:shared"))
    implementation(project(":application:monitoring"))

    api("com.eventstore:db-client-java:$event_store_version")
    implementation("io.grpc:grpc-all:$grpc_api")

    testImplementation(testFixtures(project(":application:test-utils")))

    testFixturesImplementation("org.testcontainers:testcontainers:$test_containers_version")
    testFixturesImplementation("io.insert-koin:koin-test:$koin_version")
}
