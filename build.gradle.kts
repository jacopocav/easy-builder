group = "com.github.jacopocav"
version = "0.0.1-SNAPSHOT"

plugins {
    java
    alias(libs.plugins.spotless)
}

subprojects {
    plugins.apply("java")
    plugins.apply(rootProject.libs.plugins.spotless.get().pluginId)

    spotless {
        java {
            target("**/*.java", "build/generated-sources/**/*.java")
            palantirJavaFormat(libs.versions.palantir.get())
            removeUnusedImports()
            trimTrailingWhitespace()
        }
    }

    tasks.compileJava {
        dependsOn(tasks.spotlessApply)
    }
}