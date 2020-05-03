import com.github.jengelman.gradle.plugins.shadow.tasks.*
import org.jetbrains.kotlin.gradle.tasks.*

val developmentOnly: Configuration by configurations.creating
val kotlinVersion: String by project
val micronautVersion: String by project
val micronautDataVersion: String by project
val spekVersion: String by project
val hazelcastVersion: String by project

plugins {
    val kotlinVersion = "1.3.50"
    application
    id("com.github.johnrengelman.shadow") version "5.1.0"
    id("org.jetbrains.kotlin.jvm") version kotlinVersion
    id("org.jetbrains.kotlin.kapt") version kotlinVersion
    id("org.jetbrains.kotlin.plugin.allopen") version kotlinVersion
    id("org.jetbrains.kotlin.plugin.jpa") version "1.3.71"
    id("idea")

}

idea {
    module {
        isDownloadJavadoc = true
        isDownloadSources = true
    }
}

version = "0.1"
group = "forms-manager"

repositories {
    mavenCentral()
    jcenter()
}

configurations {
    developmentOnly
}

dependencies {
    implementation(enforcedPlatform("io.micronaut:micronaut-bom:$micronautVersion"))
    compileOnly(enforcedPlatform("io.micronaut:micronaut-bom:$micronautVersion"))
    annotationProcessor(enforcedPlatform("io.micronaut:micronaut-bom:$micronautVersion"))
    testAnnotationProcessor(enforcedPlatform("io.micronaut:micronaut-bom:$micronautVersion"))
    kapt(enforcedPlatform("io.micronaut:micronaut-bom:$micronautVersion"))
    kapt("io.micronaut:micronaut-inject-java")
    kapt("io.micronaut:micronaut-validation")
    kaptTest("io.micronaut:micronaut-inject-java")

    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8:$kotlinVersion")
    implementation("org.jetbrains.kotlin:kotlin-reflect:$kotlinVersion")
    implementation("io.micronaut:micronaut-runtime")
    implementation("io.micronaut:micronaut-http-server-netty")
    implementation("io.micronaut:micronaut-http-client")
    runtimeOnly("com.fasterxml.jackson.module:jackson-module-kotlin:2.10.3")
    runtimeOnly("ch.qos.logback:logback-classic:1.2.3")

    testImplementation("org.junit.jupiter:junit-jupiter-api")
    testAnnotationProcessor("io.micronaut:micronaut-inject-java")
    testImplementation("io.micronaut.test:micronaut-test-junit5")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine")

    implementation("com.hazelcast.jet:hazelcast-jet:$hazelcastVersion")

    kapt("io.micronaut.data:micronaut-data-processor:$micronautDataVersion")
    implementation("io.micronaut.data:micronaut-data-jdbc:$micronautDataVersion")
    runtimeOnly("com.h2database:h2")
    runtimeOnly("io.micronaut.configuration:micronaut-jdbc-hikari")
    compileOnly("jakarta.persistence:jakarta.persistence-api:2.2.2")

    implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-smile:2.10.3")

    kapt("io.micronaut.configuration:micronaut-openapi:1.4.3")
    implementation("io.swagger.core.v3:swagger-annotations")

    implementation("org.apache.shiro:shiro-core:1.5.3")

    kapt("io.micronaut:micronaut-security:1.4.0")
    implementation("io.micronaut:micronaut-security-jwt:1.4.0")


}

application {
    mainClassName = "formsmanager.Application"
}

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
}

allOpen {
    annotation("io.micronaut.aop.Around")
}

tasks {
    withType<KotlinCompile> {
        kotlinOptions {
            jvmTarget = JavaVersion.VERSION_1_8.toString()
            javaParameters = true
        }
    }

    withType<Test> {
        classpath = classpath.plus(configurations["developmentOnly"])
        useJUnitPlatform()
    }

    named<JavaExec>("run") {
        doFirst {
            jvmArgs = listOf("-noverify", "-XX:TieredStopAtLevel=1", "-Dcom.sun.management.jmxremote")
            classpath = classpath.plus(configurations["developmentOnly"])
        }
    }

    named<ShadowJar>("shadowJar") {
        mergeServiceFiles()
    }
}