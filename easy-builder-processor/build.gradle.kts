import gg.jte.ContentType

plugins {
    alias(libs.plugins.jte.gradle.plugin)
}

group = parent!!.group
version = parent!!.version

repositories {
    mavenCentral()
}

dependencies {
    implementation(project(":easy-builder-annotation"))
    implementation(libs.jte.runtime)

    jteGenerate(libs.jte.models)

    testImplementation(libs.assertj.core)
    testImplementation(libs.cute)
    testImplementation(libs.junit.jupiter)
    testImplementation(libs.mockito.junit.jupiter)
    testImplementation(libs.podam)

    testRuntimeOnly(libs.junit.platform.launcher)
}

jte {
    generate()

    jteExtension("gg.jte.models.generator.ModelExtension") {
        val generatedAnnotation = "@com.github.jacopocav.builder.internal.Generated"
        property("interfaceAnnotation", generatedAnnotation)
        property("implementationAnnotation", generatedAnnotation)
    }

    sourceDirectory = projectDir.toPath().resolve("src/main/jte")
    contentType = ContentType.Plain
    trimControlStructures = true
    packageName = "com.github.jacopocav.builder.internal.template.jte"
}

tasks.spotlessJava {
    dependsOn(tasks.generateJte)
}

tasks.compileJava {
    options.compilerArgs.add("-Xlint:all")
    options.compilerArgs.add("-Xlint:-requires-automatic")

    options.annotationProcessorPath = null

    options.release = 17
}

tasks.test {
    useJUnitPlatform()
}