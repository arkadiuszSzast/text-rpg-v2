val kediatr_version: String by project

dependencies {
    api("com.trendyol:kediatr-core:$kediatr_version")
    implementation("com.trendyol:kediatr-koin-starter:$kediatr_version")

    implementation(project(":application:monitoring"))

    testImplementation(testFixtures(project(":application:test-utils")))
}
