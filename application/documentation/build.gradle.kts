val ktor_version: String by project

dependencies {
    implementation(project(":application:shared"))

    implementation("io.ktor:ktor-server-swagger-jvm:$ktor_version")
}
