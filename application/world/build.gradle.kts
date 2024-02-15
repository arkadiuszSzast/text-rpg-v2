application {
    mainClass.set("io.ktor.server.netty.EngineMain")
}

plugins {
    application
    alias(libs.plugins.ktor)
}

ktor {
    docker {
        jreVersion = JavaVersion.VERSION_21
        localImageName.set("text-rpg-ktor-world-service")
        imageTag.set("0.0.2")
        jib {
            from {
                image = "eclipse-temurin:21-jre"
            }
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

    implementation(libs.ktor.server.swagger.jvm)

    testImplementation(testFixtures(project(":application:acl")))
    testImplementation(testFixtures(project(":application:event-store")))
    testImplementation(testFixtures(project(":application:test-utils")))
}
