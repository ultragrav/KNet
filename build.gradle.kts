plugins {
    kotlin("jvm") version "1.9.21"
    kotlin("plugin.serialization") version "1.9.21"
    id("com.google.devtools.ksp") version "1.9.21-1.0.15"
    `maven-publish`
}

group = "net.ultragrav.knet"
version = "1.0.1"

repositories {
    mavenCentral()
}

dependencies {
    api("io.netty:netty-all:4.1.104.Final")
    api("net.jpountz.lz4:lz4:1.3.0")

    api("org.jetbrains.kotlinx:kotlinx-serialization-core:${properties["kotlinxSerializationVersion"]}")
    api("org.jetbrains.kotlinx:kotlinx-serialization-protobuf:${properties["kotlinxSerializationVersion"]}")
    api("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")

    api(project(":Processor"))
    ksp(project(":Processor"))
}

publishing {
    publications {
        register("mavenJava", MavenPublication::class) {
            from(components["java"])
        }
    }
}
