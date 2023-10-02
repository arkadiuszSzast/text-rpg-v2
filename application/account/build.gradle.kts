dependencies {
    implementation(project(":application:shared"))
    implementation(project(":application:security"))
    implementation(project(":application:monitoring"))
    implementation(project(":application:mediator"))
    implementation(project(":application:documentation"))
    implementation(project(":application:event-store"))
    implementation(project(":application:acl"))
    implementation(project(":application:mail"))

    testImplementation(testFixtures(project(":application:event-store")))
    testImplementation(testFixtures(project(":application:acl")))
    testImplementation(testFixtures(project(":application:mail")))
    testImplementation(testFixtures(project(":application:test-utils")))
    testImplementation(testFixtures(project(":application:shared")))
}
