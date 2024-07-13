plugins {
    java
}

group = parent!!.group
version = parent!!.version

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(libs.junit.jupiter)
    testImplementation(libs.guava)
    testImplementation(libs.assertj.core)

    testRuntimeOnly(libs.junit.platform.launcher)
}

tasks.compileJava {
    options.compilerArgs.add("-Xlint:all")
}

tasks.test {
    useJUnitPlatform()
}