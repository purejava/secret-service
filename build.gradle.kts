import net.thebugmc.gradle.sonatypepublisher.PublishingType.*

plugins {
    id("java-library")
    id("net.thebugmc.gradle.sonatype-central-portal-publisher") version "1.2.4"
    id("com.github.breadmoirai.github-release") version "2.5.2"
    id("maven-publish")
    id("signing")
    id("com.adarshr.test-logger") version "4.0.0"
}

group = "org.purejava"
version = "1.0.1-SNAPSHOT"
description = "A Java library for managing secrets on Linux using the secret service DBus interface"

val releaseGradlePluginToken: String = System.getenv("RELEASE_GRADLE_PLUGIN_TOKEN") ?: ""
val sonatypeUsername: String = System.getenv("SONATYPE_USERNAME") ?: ""
val sonatypePassword: String = System.getenv("SONATYPE_PASSWORD") ?: ""

java {
    java.sourceCompatibility = JavaVersion.VERSION_21
    withSourcesJar()
    withJavadocJar()
}

repositories {
    mavenLocal()
    mavenCentral()
}

dependencies {
    api(libs.com.github.hypfvieh.dbus.java.core)
    api(libs.com.github.hypfvieh.dbus.java.transport.native.unixsocket)
    api(libs.at.favre.lib.hkdf)
    api(libs.org.slf4j.slf4j.api)
    testImplementation(libs.org.junit.jupiter.junit.jupiter.api)
    testImplementation(libs.org.junit.jupiter.junit.jupiter.engine)
    testImplementation(libs.org.junit.jupiter.junit.jupiter)
    testImplementation(libs.org.slf4j.slf4j.simple)
    testRuntimeOnly(libs.org.junit.platform.junit.platform.launcher)
}

tasks.test {
    useJUnitPlatform()
    filter {
        includeTestsMatching("*Test")
    }
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])

            pom {
                name.set("secret-service")
                description.set("A Java library for managing secrets on Linux using the secret service DBus interface")
                url.set("https://github.com/purejava/secret-service")

                licenses {
                    license {
                        name.set("MIT License")
                        url.set("https://opensource.org/licenses/MIT")
                    }
                }

                developers {
                    developer {
                        id.set("purejava")
                        name.set("Ralph Plawetzki")
                        email.set("ralph@purejava.org")
                    }
                }

                scm {
                    connection.set("scm:git:git://github.com/purejava/secret-service.git")
                    developerConnection.set("scm:git:ssh://github.com/purejava/secret-service.git")
                    url.set("https://github.com/purejava/secret-service/tree/main")
                }

                issueManagement {
                    system.set("GitHub Issues")
                    url.set("https://github.com/purejava/secret-service/issues")
                }
            }
        }
    }
}

centralPortal {
    publishingType.set(USER_MANAGED)

    username.set(sonatypeUsername)
    password.set(sonatypePassword)

    // Configure POM metadata
    pom {
        name.set("secret-service")
        description.set("A Java library for managing secrets on Linux using the secret service DBus interface")
        url.set("https://github.com/purejava/secret-service")
        licenses {
            license {
                name.set("MIT License")
                url.set("https://opensource.org/licenses/MIT")
            }
        }
        developers {
            developer {
                id.set("purejava")
                name.set("Ralph Plawetzki")
                email.set("ralph@purejava.org")
            }
        }
        scm {
            connection.set("scm:git:git://github.com/purejava/secret-service.git")
            developerConnection.set("scm:git:ssh://github.com/purejava/secret-service.git")
            url.set("https://github.com/purejava/secret-service/tree/main")
        }
        issueManagement {
            system.set("GitHub Issues")
            url.set("https://github.com/purejava/secret-service/issues")
        }
    }
}

githubRelease {
    repo = "secret-service"
    token(releaseGradlePluginToken)
    tagName = project.version.toString()
    releaseName = project.version.toString()
    targetCommitish = "main"
    draft = true
    generateReleaseNotes = true
}

tasks.named("publishCentralPortalPublicationToMavenLocal") {
    dependsOn("signMavenJavaPublication")
}

tasks.named("publishMavenJavaPublicationToMavenLocal") {
    dependsOn("signCentralPortalPublication")
}

signing {
    useGpgCmd()
    sign(configurations.runtimeElements.get())
    sign(publishing.publications["mavenJava"])
}

tasks.withType<JavaCompile>().configureEach {
    options.encoding = "UTF-8"
}

tasks.withType<PublishToMavenLocal>().configureEach {
    if (publication.name == "centralPortal") {
        enabled = false
    }
}

tasks.withType<Javadoc> {
    isFailOnError = false
    if (JavaVersion.current().isJava9Compatible) {
        (options as? StandardJavadocDocletOptions)?.addBooleanOption("html5", true)
    }
    (options as? StandardJavadocDocletOptions)?.encoding = "UTF-8"
}
