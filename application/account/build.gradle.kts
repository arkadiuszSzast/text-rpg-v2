application {
    mainClass.set("io.ktor.server.netty.EngineMain")
}

plugins {
    application
    alias(libs.plugins.ktor)
}

ktor {
    docker {
        localImageName.set("text-rpg-ktor-account-service")
        imageTag.set("0.0.5")
        jreVersion = JavaVersion.VERSION_21
        jib {
            extraDirectories {
                paths {
                    path {
                        setFrom("../../.docker/open-telemetry")
                        into = "/app/libs/opentelemetry/"
                    }
                }
            }
            container {
                creationTime = "USE_CURRENT_TIMESTAMP"
                jvmFlags = listOf("-javaagent:/app/libs/opentelemetry/opentelemetry-javaagent.jar")
            }
        }
    }
}

dependencies {
    implementation(project(":application:shared"))
    implementation(project(":application:security"))
    implementation(project(":application:monitoring"))
    implementation(project(":application:mediator"))
    implementation(project(":application:event-store"))
    implementation(project(":application:acl"))
    implementation(project(":application:mail"))
    implementation(project(":application:redis"))

    implementation(libs.ktor.server.swagger.jvm)

    testImplementation(testFixtures(project(":application:event-store")))
    testImplementation(testFixtures(project(":application:acl")))
    testImplementation(testFixtures(project(":application:mail")))
    testImplementation(testFixtures(project(":application:test-utils")))
    testImplementation(testFixtures(project(":application:shared")))
    testImplementation(testFixtures(project(":application:redis")))
}
