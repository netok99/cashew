import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.ktor)
    alias(libs.plugins.dokka)
    alias(libs.plugins.detekt)
    alias(libs.plugins.kover)
    alias(libs.plugins.kotlinx.serialization)
    alias(libs.plugins.sqldelight)
}

group = "com"
version = "0.0.1"

application {
    mainClass.set("io.ktor.server.netty.EngineMain")

    val isDevelopment: Boolean = project.ext.has("development")
    applicationDefaultJvmArgs = listOf("-Dio.ktor.development=$isDevelopment")
}

repositories {
    mavenCentral()
}

tasks {
    withType<KotlinCompile>().configureEach {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_12)
            freeCompilerArgs.add("-Xcontext-receivers")
        }
    }

    test {
        useJUnitPlatform()
    }
}

ktor {
    docker {
        jreVersion = JavaVersion.VERSION_12
        localImageName = "ktor-arrow-example"
        imageTag = "latest"
    }
}

kotlin {
    jvmToolchain(12)
}

dependencies {
    implementation(libs.bundles.arrow)
    implementation(libs.bundles.ktor.server)
    implementation(libs.bundles.suspendapp)
    implementation(libs.logback.classic)
    testImplementation(libs.ktor.server.test.host)
    testImplementation(libs.kotlin.test.junit)

    implementation(libs.hikari)
    implementation(libs.postgresql)
    implementation(libs.bundles.cohort)
    testImplementation(libs.testcontainers.postgresql)
}
