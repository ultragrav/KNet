plugins {
    kotlin("jvm") version "1.9.21"
    kotlin("plugin.serialization") version "1.9.21"
    id("com.google.devtools.ksp") version "1.9.21-1.0.15"
}

group = "net.ultragrav"
version = "1.0.1"

repositories {
    mavenCentral()
}

dependencies {
    api(project(":API"))

    implementation("io.netty:netty-all:4.1.104.Final")
    implementation("net.jpountz.lz4:lz4:1.3.0")

    implementation("org.jetbrains.kotlinx:kotlinx-serialization-core:${properties["kotlinxSerializationVersion"]}")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-protobuf:${properties["kotlinxSerializationVersion"]}")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")

    ksp(project(":Processor"))
}
