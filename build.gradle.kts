import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.springframework.boot.gradle.tasks.bundling.BootJar

plugins {
    id("org.springframework.boot") version "3.0.10"
    id("com.google.protobuf") version "0.9.4"
    id("io.spring.dependency-management") version "1.1.3"
    id("io.gitlab.arturbosch.detekt") version ("1.21.0")
    kotlin("jvm") version "1.7.22"
    kotlin("plugin.noarg") version "1.7.22"
    kotlin("plugin.spring") version "1.7.22"
    kotlin("plugin.jpa") version "1.7.22"
    application
    java
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
}

configurations {
    compileOnly {
        extendsFrom(configurations.annotationProcessor.get())
    }
}

allprojects {
    group = "rys.ajaxpetproject"
    version = "0.0.1-SNAPSHOT"
    repositories {
        mavenCentral()
    }
}

dependencies {
    implementation(project(":nats"))
    implementation(project(":core"))
    implementation(project(":rest"))
    implementation(project(":api"))
    implementation("org.springframework.boot:spring-boot-devtools")
}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        freeCompilerArgs += "-Xjsr305=strict"
        jvmTarget = "17"
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}

subprojects {
    apply(plugin = "org.springframework.boot")
    apply(plugin = "io.spring.dependency-management")
    apply(plugin = "io.gitlab.arturbosch.detekt")
    apply(plugin = "org.jetbrains.kotlin.jvm")
    apply(plugin = "org.jetbrains.kotlin.plugin.spring")
    apply(plugin = "org.jetbrains.kotlin.plugin.jpa")
    apply(plugin = "org.jetbrains.kotlin.plugin.noarg")
    apply(plugin = "java")
    apply(plugin = "com.google.protobuf")

    java {
        sourceCompatibility = JavaVersion.VERSION_17
    }

    tasks.getByName<BootJar>("bootJar") {
        enabled = false
    }

    configurations {
        compileOnly {
            extendsFrom(configurations.annotationProcessor.get())
        }
    }

    dependencies {
        implementation("org.springframework.boot:spring-boot-starter-data-mongodb-reactive:3.1.4")
        implementation("org.springframework.boot:spring-boot-starter-web")
        implementation("org.springframework.boot:spring-boot-starter-logging:3.1.0")
        implementation("org.springframework.boot:spring-boot-starter-validation")
        implementation("org.springframework.data:spring-data-jpa:3.0.9")

        implementation("org.springframework.security:spring-security-crypto:6.1.2")
        implementation("jakarta.validation:jakarta.validation-api:3.0.2")
        implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
        implementation("org.jetbrains.kotlin:kotlin-reflect")
        implementation("com.fasterxml.jackson.module:jackson-module-kotlin")

        implementation("com.google.protobuf:protobuf-java:3.24.3")
        implementation("io.nats:jnats:2.17.0")

        implementation("io.projectreactor:reactor-core")
        implementation("io.projectreactor.kotlin:reactor-kotlin-extensions")

        testImplementation("io.mockk:mockk:1.13.8")
        testImplementation("io.projectreactor:reactor-test")
        testImplementation("org.junit.jupiter:junit-jupiter")
        testImplementation("org.springframework.boot:spring-boot-starter-test")
    }

    tasks.withType<KotlinCompile> {
        kotlinOptions {
            freeCompilerArgs += "-Xjsr305=strict"
            jvmTarget = "17"
        }
    }

    tasks.withType<Test> {
        useJUnitPlatform()
    }
}
