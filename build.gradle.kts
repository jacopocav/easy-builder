plugins {
    id("java")
}

group = "com.github.jacopocav"
version = "0.0.1-SNAPSHOT"

repositories {
    mavenCentral()
}

tasks.withType<JavaCompile> {
    sourceCompatibility = "17"
    targetCompatibility = "17"

    options.compilerArgs.add("-Werror")
    options.compilerArgs.add("-Xlint:all")

    options.annotationProcessorPath = null
}

tasks.test {
    useJUnitPlatform()
}