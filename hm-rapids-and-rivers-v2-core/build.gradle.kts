import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile


val jvmTarget = "17"
val kafkaVersion = "3.3.1"
val micrometerRegistryPrometheusVersion = "1.9.1"
val junitJupiterVersion = "5.9.1"
val jacksonVersion = "2.14.1"
val logbackClassicVersion = "1.4.5"
val logbackEncoderVersion = "7.2"
val kafkaEmbeddedVersion = "3.2.1"
val awaitilityVersion = "4.2.0"

group = "com.github.navikt"
version = properties["version"] ?: "local-build"

plugins {
    kotlin("jvm") version "1.7.0"
    id("java")
    id("maven-publish")
}

dependencies {
    api("ch.qos.logback:logback-classic:$logbackClassicVersion")
    api("net.logstash.logback:logstash-logback-encoder:$logbackEncoderVersion")

    api("org.apache.kafka:kafka-clients:$kafkaVersion")

    api("com.fasterxml.jackson.module:jackson-module-kotlin:$jacksonVersion")
    api("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:$jacksonVersion")

    api("io.micrometer:micrometer-registry-prometheus:$micrometerRegistryPrometheusVersion")

    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-core-jvm:1.6.4")

    testImplementation("org.junit.jupiter:junit-jupiter-api:$junitJupiterVersion")
    testImplementation("org.junit.jupiter:junit-jupiter-params:$junitJupiterVersion")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:$junitJupiterVersion")

    testImplementation("no.nav:kafka-embedded-env:$kafkaEmbeddedVersion"){
        exclude("log4j")
        exclude("org.glassfish")
        exclude("io.netty")
    }
    testImplementation("org.awaitility:awaitility:$awaitilityVersion")
}

java {
    sourceCompatibility = JavaVersion.toVersion(jvmTarget)
    targetCompatibility = JavaVersion.toVersion(jvmTarget)

    withSourcesJar()
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = jvmTarget
}

tasks.named<KotlinCompile>("compileTestKotlin") {
    kotlinOptions.jvmTarget = jvmTarget
}

tasks.withType<Test> {
    useJUnitPlatform()
    testLogging {
        events("skipped", "failed")
        showExceptions = true
        showStackTraces = true
        showCauses = true
        exceptionFormat = TestExceptionFormat.FULL
        showStandardStreams = true
    }
}

tasks.withType<Wrapper> {
    gradleVersion = "7.5.1"
}

repositories {
    mavenLocal()
    mavenCentral()
    maven("https://packages.confluent.io/maven/")

}

val githubUser: String? by project
val githubPassword: String? by project

publishing {
    repositories {
        maven {
            url = uri("https://maven.pkg.github.com/navikt/hm-rapids-and-rivers-v2")
            credentials {
                username = githubUser
                password = githubPassword
            }
        }
    }
    publications {
        create<MavenPublication>("mavenJava") {

            pom {
                name.set("hm-rapids-rivers-v2-core")
                description.set("hm rapids and rivers v2")
                url.set("https://github.com/navikt/hm-rapids-and-rivers-v2")

                licenses {
                    license {
                        name.set("MIT License")
                        url.set("https://opensource.org/licenses/MIT")
                    }
                }

                scm {
                    connection.set("scm:git:https://github.com/navikt/hm-rapids-and-rivers-v2.git")
                    developerConnection.set("scm:git:https://github.com/navikt/hm-rapids-and-rivers-v2.git")
                    url.set("https://github.com/navikt/hm-rapids-and-rivers-v2")
                }
            }
            from(components["java"])
        }
    }
}
