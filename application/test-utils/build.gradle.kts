val opentelemetry_version: String by project

dependencies {
  testFixturesApi("io.opentelemetry:opentelemetry-sdk-testing:$opentelemetry_version")
}
