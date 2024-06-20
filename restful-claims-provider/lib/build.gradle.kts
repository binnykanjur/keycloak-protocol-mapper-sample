plugins {
    `java-library`
}

group = "com.binnykanjur.keycloak.protocolmapper"
version = "1.0.0"
description = "Keycloak RESTful Claims Provider"

java {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.json:json:20240303")
    implementation("org.keycloak:keycloak-core:25.0.0")
    implementation("org.keycloak:keycloak-services:25.0.0")
    implementation("org.keycloak:keycloak-server-spi:25.0.0")
    implementation("org.keycloak:keycloak-server-spi-private:25.0.0")
}

tasks {
    val copyToLib by creating(Copy::class) {
        into("$rootDir/../keycloak/providers")
        from(configurations.runtimeClasspath) {
            include("json-*")
        }
    }

    jar {
        dependsOn(copyToLib)
        destinationDirectory = File("$rootDir/../keycloak/providers")
        archiveFileName.set("${rootProject.name}-${project.version}.jar")
    }
}
