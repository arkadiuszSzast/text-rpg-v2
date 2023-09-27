val arrow_version: String by project
val ktor_version: String by project
val opentelemetry_version: String by project

dependencies {
  implementation(project(":application:shared"))
  implementation(project(":application:event-store"))
  implementation(project(":application:monitoring"))
  implementation("io.ktor:ktor-client-core:$ktor_version")
  implementation("io.ktor:ktor-client-content-negotiation:$ktor_version")
  implementation("io.ktor:ktor-client-logging:$ktor_version")
  implementation("io.ktor:ktor-client-cio:$ktor_version")

  implementation("io.ktor:ktor-client-logging-jvm:$ktor_version")

  testImplementation(testFixtures(project(":application:test-utils")))
  testImplementation(testFixtures(project(":application:event-store")))
  testImplementation(testFixtures(project(":application:shared")))
  testImplementation("io.opentelemetry:opentelemetry-sdk-testing:$opentelemetry_version")
  testImplementation("io.ktor:ktor-client-mock:$ktor_version")

  testFixturesImplementation(project(":application:event-store"))
  testFixturesImplementation("io.arrow-kt:arrow-core:$arrow_version")
}
