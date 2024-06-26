import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

buildscript {
    repositories {
        mavenCentral()
    }
}

allprojects {
    apply(plugin = "kotlin")

    repositories {
        mavenCentral()
    }

    tasks.withType<KotlinCompile> {
        kotlinOptions {
            jvmTarget = "20"
        }
    }
    tasks.withType<JavaCompile> {
        sourceCompatibility = "20"
        targetCompatibility = "20"
    }
}