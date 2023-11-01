import com.google.protobuf.gradle.id

dependencies {
    implementation("com.google.protobuf:protobuf-java:3.24.3")
    implementation("javax.annotation:javax.annotation-api:1.3.2")
}

protobuf {
    protoc {
        artifact = "com.google.protobuf:protoc:3.24.3"
    }

    plugins {
        id("grpc") {
            artifact = "io.grpc:protoc-gen-grpc-java:1.46.0"
        }
        id("reactor-grpc") {
            artifact = "com.salesforce.servicelibs:reactor-grpc:1.2.4"
        }
    }
    generateProtoTasks {
        all().forEach {
            it.plugins {
                create("grpc")
                create("reactor-grpc")
            }
        }
    }

}
