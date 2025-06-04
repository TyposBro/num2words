import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    kotlin("jvm") version "2.0.0"
    `java-library`
    `maven-publish`
}

group = "io.github.typosbro"
version = "0.1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
    withJavadocJar()
    withSourcesJar()
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().all {
    compilerOptions.jvmTarget.set(JvmTarget.JVM_1_8)
}

dependencies {
    testImplementation(kotlin("test"))
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.10.0")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.10.0")
}

tasks.test {
    useJUnitPlatform()
}


kotlin {
    jvmToolchain(11)
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])
            pom {
                name.set("Kotlin Num2Words Library")
                description.set("A Kotlin library to convert numbers to words.")
                url.set("https://github.com/typosbro/num2words")
                 licenses {
                     license {
                         name.set("The Apache License, Version 2.0")
                         url.set("https://www.apache.org/licenses/LICENSE-2.0.txt")
                     }
                 }
                 developers {
                     developer {
                         id.set("typosbro")
                         name.set("Typosbro")
                         email.set("typosbro@proton.me")
                     }
                 }
                 scm {
                     connection.set("scm:git:git://github.com/typosbro/num2words.git")
                     developerConnection.set("scm:git:ssh://github.com/typosbro/num2words.git")
                     url.set("https://github.com/typosbro/num2words/tree/main")
                 }
            }
        }
    }
    repositories {
        maven {
            name = "GitHubPackages"
            url = uri("https://maven.pkg.github.com/typosbro/num2words")
            credentials {
                username = System.getenv("GITHUB_USER") ?: project.findProperty("gpr.user")?.toString()
                password = System.getenv("GITHUB_TOKEN") ?: project.findProperty("gpr.key")?.toString()
            }
        }
    }
}