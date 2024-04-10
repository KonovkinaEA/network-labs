plugins {
    kotlin("jvm")
    application
}

group = "uni.lizsa"
version = "unspecified"

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.8.0")
    testImplementation("org.jetbrains.kotlin:kotlin-test")
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(20)
}

application {
    mainClass.set("uni.lizsa.smtp.MainKt")
}
