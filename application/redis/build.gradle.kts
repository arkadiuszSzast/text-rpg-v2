dependencies {
  api(libs.redisson)

  implementation((project(":application:shared")))

  testFixturesImplementation(libs.testcontainers)
}
