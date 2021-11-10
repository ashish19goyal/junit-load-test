/*
 */

plugins {
    // Apply the application plugin to add support for building a CLI application in Java.
    application
    `maven-publish`
}

repositories {
    // Use Maven Central for resolving dependencies.
    mavenCentral()
    mavenLocal()
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            groupId = "junit.load.test"
            artifactId = "junit-load-test"
            version = "1.0-SNAPSHOT"

            from(components["java"])
        }
    }
}

dependencies {
    // Use JUnit Jupiter API for testing.
    implementation("org.junit.jupiter:junit-jupiter-api:5.7.2")

    // https://mvnrepository.com/artifact/org.junit.platform/junit-platform-launcher
    implementation("org.junit.platform:junit-platform-launcher:1.7.2")

    // Use JUnit Jupiter Engine for testing.
    implementation("org.junit.jupiter:junit-jupiter-engine:5.7.2")

    // https://mvnrepository.com/artifact/com.fasterxml.jackson.core/jackson-databind
    implementation("com.fasterxml.jackson.core:jackson-databind:2.0.1")

    // https://mvnrepository.com/artifact/org.apache.commons/commons-math3
    implementation("org.apache.commons:commons-math3:3.0")
}

//tasks.register<Test>("loadtest") {
//    useJUnitPlatform {
//        includeEngines("load-test-engine")
//    }
//}
