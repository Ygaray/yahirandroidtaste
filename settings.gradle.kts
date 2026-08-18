pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}
plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

// D-01 (topology): single-module hub — the repo root IS the publishable `:yahirandroidtaste`
// Android library (build.gradle.kts + src/ at the root), so there is no redundant
// `:yahirandroidtaste:yahirandroidtaste` nesting and every doc/jitpack build command drops the
// module prefix.
rootProject.name = "yahirandroidtaste"
