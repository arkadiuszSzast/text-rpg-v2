application {
    mainClass.set("io.ktor.server.netty.EngineMain")
}

plugins {
    application
    alias(libs.plugins.ktor)
}

ktor {
    docker {
        localImageName.set("text-rpg-ktor")
        imageTag.set("0.0.1")
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
    implementation(project(":application:account"))
    implementation(project(":application:world"))
}
