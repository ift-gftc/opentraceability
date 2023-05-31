plugins {
    kotlin("jvm") version "1.8.21"
    application
}

group = "org.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation("com.google.code.gson:gson:2.7")
    implementation("com.fasterxml.jackson.core:jackson-databind:2.0.1")
    implementation("org.json:json:20211205")
    implementation("org.wso2.orbit.org.everit.json:org.everit.json.schema:1.5.0.wso2.v2")
    implementation("org.jetbrains.kotlin:kotlin-reflect:1.4.21")


    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(11)
}

application {
    mainClass.set("MainKt")
}