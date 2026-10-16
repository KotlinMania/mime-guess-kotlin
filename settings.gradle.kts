pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

plugins { id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0" }

dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "mime-guess-kotlin"

val mimeKotlinLocal = file("../mime-kotlin")
if (mimeKotlinLocal.exists()) {
    includeBuild(mimeKotlinLocal) {
        dependencySubstitution {
            substitute(module("io.github.kotlinmania:mime-kotlin"))
                .using(project(":"))
        }
    }
}
