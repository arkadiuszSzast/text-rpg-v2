val redisson_version: String by project
val test_containers_version: String by project

dependencies {
  api("org.redisson:redisson:$redisson_version")

  implementation((project(":application:shared")))

  testFixturesImplementation("org.testcontainers:testcontainers:$test_containers_version")
}
