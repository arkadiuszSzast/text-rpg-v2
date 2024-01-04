dependencies {
    implementation(project(":application:shared"))
    implementation(project(":application:monitoring"))

    api(libs.eventstore)
    implementation(libs.grpc.all)
    implementation(libs.jackson.core)

    testImplementation(testFixtures(project(":application:test-utils")))

    testFixturesImplementation(libs.testcontainers)
    testFixturesImplementation(libs.koin.test)
    testFixturesImplementation(libs.kotest.framework.api)
}
