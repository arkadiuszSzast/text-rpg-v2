dependencies {
  implementation(project(":application:shared"))
  implementation(project(":application:event-store"))
  implementation(project(":application:monitoring"))
  implementation(libs.ktor.client.core)
  implementation(libs.ktor.client.content.negotiation)
  implementation(libs.ktor.client.logging)
  implementation(libs.ktor.client.cio)

  testImplementation(testFixtures(project(":application:test-utils")))
  testImplementation(testFixtures(project(":application:event-store")))
  testImplementation(testFixtures(project(":application:shared")))
  testImplementation(libs.opentelemetry.sdk.testing)
  testImplementation(libs.ktor.client.mock)

  testFixturesImplementation(project(":application:event-store"))
  testFixturesImplementation(libs.arrow.core)
}
