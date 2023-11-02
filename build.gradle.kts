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
        maven {
            setUrl("https://packages.confluent.io/maven/")
        }
    }
}

dependencies {
    implementation(project(":nats"))
    implementation(project(":core"))
    implementation(project(":rest"))
    implementation(project(":api"))
    implementation(project(":kafka"))
    implementation(project(":gRPC"))
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

    configurations.all {
        exclude(group = "org.slf4j", module = "slf4j-reload4j")
    }

    apply(plugin = "org.jetbrains.kotlin.jvm")
    apply(plugin = "org.jetbrains.kotlin.plugin.spring")
    apply(plugin = "org.springframework.boot")
    apply(plugin = "io.spring.dependency-management")
    apply(plugin = "com.google.protobuf")
    apply(plugin = "io.gitlab.arturbosch.detekt")

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
        implementation("org.springframework.boot:spring-boot-starter-data-mongodb-reactive")
        implementation("org.springframework.boot:spring-boot-starter-webflux")
        implementation("org.springframework.boot:spring-boot-starter-validation")
        implementation("net.devh:grpc-spring-boot-starter:2.15.0.RELEASE")
        implementation("net.devh:grpc-server-spring-boot-starter:2.15.0.RELEASE")

        implementation("com.salesforce.servicelibs:reactor-grpc:1.2.4")
        implementation("com.salesforce.servicelibs:reactive-grpc-common:1.2.4")
        implementation("com.salesforce.servicelibs:reactor-grpc-stub:1.2.4")

        implementation("io.confluent:kafka-schema-registry:7.5.1")
        implementation("io.confluent:kafka-protobuf-serializer:7.5.1")
        implementation("io.projectreactor.kafka:reactor-kafka:1.3.19")
        implementation("org.springframework.kafka:spring-kafka")

        implementation("org.springframework.security:spring-security-crypto")
        implementation("jakarta.validation:jakarta.validation-api:3.0.2")
        implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
        implementation("org.jetbrains.kotlin:kotlin-reflect")
        implementation("com.fasterxml.jackson.module:jackson-module-kotlin")

        implementation("com.google.protobuf:protobuf-java:3.24.3")
        implementation("io.nats:jnats:2.17.0")

        implementation("io.projectreactor:reactor-core")
        implementation("io.projectreactor.kotlin:reactor-kotlin-extensions")

        testImplementation("io.mockk:mockk:1.13.8")
        testImplementation("org.mockito.kotlin:mockito-kotlin:3.2.0")
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
