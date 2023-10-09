import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("org.springframework.boot") version "3.0.10"
    id("io.spring.dependency-management") version "1.1.3"
    id("io.gitlab.arturbosch.detekt") version("1.21.0")
    id("com.google.protobuf") version "0.9.4"
    kotlin("jvm") version "1.7.22"
    kotlin("plugin.spring") version "1.7.22"
    kotlin("plugin.jpa") version "1.7.22"
    kotlin("plugin.noarg") version "1.7.22"
    application
    java


}

group = "rys"
version = "0.0.1-SNAPSHOT"

java {
    sourceCompatibility = JavaVersion.VERSION_17
}

configurations {
    compileOnly {
        extendsFrom(configurations.annotationProcessor.get())
    }
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(project(":rest"))
    implementation(project(":nats"))
    implementation("io.nats:jnats:2.14.0")
    implementation("com.google.protobuf:protobuf-java:3.24.3")
    implementation("com.google.protobuf:protobuf-java-util:3.24.3")
    implementation("org.springframework.boot:spring-boot-devtools")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.security:spring-security-crypto:6.1.2")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("jakarta.validation:jakarta.validation-api:3.0.2")
    implementation("org.springframework.data:spring-data-jpa:3.0.9")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("org.springframework.boot:spring-boot-starter-data-mongodb:3.0.10")
    implementation("org.springframework.boot:spring-boot-starter-logging:3.1.0")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
}

springBoot {
    mainClass.set("rys.ajaxpetproject.AjaxPetProjectApplicationKt")
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

noArg {
    annotation("org.springframework.web.bind.annotation.RestController")
}

subprojects {
    apply(plugin = "org.springframework.boot")
    apply(plugin = "io.spring.dependency-management")
    apply(plugin = "io.gitlab.arturbosch.detekt")
    apply(plugin = "org.jetbrains.kotlin.jvm")
    apply(plugin = "org.jetbrains.kotlin.plugin.spring")
    apply(plugin = "org.jetbrains.kotlin.plugin.jpa")
    apply(plugin = "org.jetbrains.kotlin.plugin.noarg")
    apply(plugin = "application")
    apply(plugin = "java")
    apply(plugin = "com.google.protobuf")

    group = "rys"
    version = "0.0.1-SNAPSHOT"

    java {
        sourceCompatibility = JavaVersion.VERSION_17
    }

    configurations {
        compileOnly {
            extendsFrom(configurations.annotationProcessor.get())
        }
    }

    repositories {
        mavenCentral()
    }

    dependencies {
        testImplementation("org.mockito.kotlin:mockito-kotlin:3.2.0")
        implementation("com.google.protobuf:protobuf-java:3.24.3")
        implementation("org.springframework.boot:spring-boot-starter-web")
        implementation("org.springframework.security:spring-security-crypto:6.1.2")
        implementation("org.springframework.boot:spring-boot-starter-validation")
        implementation("jakarta.validation:jakarta.validation-api:3.0.2")
        implementation("org.springframework.data:spring-data-jpa:3.0.9")
        implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
        implementation("org.jetbrains.kotlin:kotlin-reflect")
        testImplementation("org.springframework.boot:spring-boot-starter-test")
        implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
        implementation("io.nats:jnats:2.17.0")
        implementation("org.springframework.boot:spring-boot-starter-data-mongodb:3.0.10")
        implementation("org.springframework.boot:spring-boot-starter-logging:3.1.0")
        implementation("org.projectlombok:lombok")
        annotationProcessor("org.projectlombok:lombok")
        testImplementation("org.springframework.boot:spring-boot-starter-test")
    }

    springBoot {
        mainClass.set("rys.ajaxpetproject.AjaxPetProjectApplicationKt")
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

    noArg {
        annotation("org.springframework.web.bind.annotation.RestController")
    }
}
