import org.gradle.api.tasks.testing.logging.TestLogEvent.*

plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.ktor)
    alias(libs.plugins.kotlin.plugin.serialization)
}

group = "xyz.avdt"
version = "0.0.1"

application {
    mainClass = "io.ktor.server.cio.EngineMain"
}

kotlin {
    jvmToolchain(17)
}

jib {
    from {
        image = "eclipse-temurin:17-jre"
        val linux = "linux"
        platforms {
            platform {
                architecture = "arm64"
                os = linux
            }
            platform {
                architecture = "amd64"
                os = linux
            }
        }
    }
}

ktor {
    docker {
        localImageName.set("shrtkt-api")
        imageTag.set(version.toString())
        jreVersion.set(JavaVersion.VERSION_17)
    }
}

dependencies {
    implementation(libs.ktor.server.core)
    implementation(libs.ktor.serialization.kotlinx.json)
    implementation(libs.ktor.server.content.negotiation)
    implementation(libs.exposed.core)
    implementation(libs.exposed.jdbc)
    implementation(libs.h2)
    implementation(libs.ktor.server.metrics)
    implementation(libs.ktor.server.call.logging)
    implementation(libs.ktor.server.resources)
    implementation(libs.ktor.server.cors)
    implementation(libs.ktor.server.cio)
    implementation(libs.logback.classic)
    implementation(libs.ktor.server.config.yaml)
    testImplementation(libs.ktor.server.test.host)
    testImplementation(libs.kotlin.test.junit)
}

repositories {
    mavenCentral()
}

tasks.test {
    testLogging {
        events = setOf(PASSED, FAILED, SKIPPED)
    }
}

