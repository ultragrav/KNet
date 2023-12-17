plugins {
    kotlin("jvm")
}

repositories {
    mavenCentral()
}

dependencies {
    api(project(":API"))

    implementation("com.google.devtools.ksp:symbol-processing-api:1.9.21-1.0.15")
}