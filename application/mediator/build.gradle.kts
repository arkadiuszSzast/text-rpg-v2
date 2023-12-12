dependencies {
    api(libs.kediatr.core)
    implementation(libs.kediatr.koin)

    implementation(project(":application:monitoring"))

    testImplementation(testFixtures(project(":application:test-utils")))
}
