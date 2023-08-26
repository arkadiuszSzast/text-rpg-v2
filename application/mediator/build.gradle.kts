val kediatr_version: String by project
val opentelemetry_version: String by project

dependencies {
    implementation("com.trendyol:kediatr-core:$kediatr_version")
    implementation("com.trendyol:kediatr-koin-starter:$kediatr_version")

    implementation(project(":application:monitoring"))

    testImplementation("io.opentelemetry:opentelemetry-sdk-testing:$opentelemetry_version")
}
