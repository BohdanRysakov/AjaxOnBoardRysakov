dependencies {
    implementation(project(":core"))
    implementation(project(":api"))
    implementation("com.google.protobuf:protobuf-java-util:3.24.3")

    testImplementation("org.junit.jupiter:junit-jupiter-api")
    testImplementation(project(":kafka"))
}
