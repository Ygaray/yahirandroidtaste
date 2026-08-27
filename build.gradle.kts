plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.hilt)
    alias(libs.plugins.ksp)
    alias(libs.plugins.detekt)
    alias(libs.plugins.metalava)
    `maven-publish`
}

// Mechanism 3 (Metalava, spike Task 1): mechanisms 1 (Kotlin built-in abiValidation) and 2
// (classic org.jetbrains.kotlinx.binary-compatibility-validator) do not work on this AGP-9
// built-in-Kotlin / Compose stack — see tools/README-api-guard.md for the full spike record.
// Metalava's release-variant signature dump is emitted to $rootDir/api.txt (root-as-module).
// apiDump/apiCheck below are the stable, tool-agnostic task names downstream guards depend on.
metalava {
    filename = "api.txt"
}

tasks.register("apiDump") {
    group = "verification"
    description = "Regenerates the committed public-API signature file (delegates to Metalava)."
    dependsOn("metalavaGenerateSignatureRelease")
}

tasks.register("apiCheck") {
    group = "verification"
    description = "Checks the current public API against the committed signature file (delegates to Metalava)."
    dependsOn("metalavaCheckCompatibilityRelease")
}

detekt {
    // D-05 (corrected policy, SecondBrain Phase 47): layer the shared strict config THEN the
    // per-module Compose-idiomatic size-threshold override (later file wins on merged keys).
    // Both configs now live in this repo's own config/ tree — $rootDir == $projectDir here
    // (root-as-module, D-01), so the paths carried over from SecondBrain resolve unchanged.
    config.setFrom(files("$rootDir/config/detekt/detekt.yml", "$projectDir/config/detekt-compose.yml"))
    baseline = file("$projectDir/config/detekt-baseline.xml")
    buildUponDefaultConfig = false
    allRules = false
}

android {
    // Package renamed to io.github.ygaray.yahirandroidtaste in Phase 101 (LIB-03) —
    // publisher-owned, consumer-name-free. The JitPack coordinate (com.github.Ygaray) is unaffected.
    namespace = "io.github.ygaray.yahirandroidtaste"
    compileSdk {
        version = release(36) {
            minorApiLevel = 1
        }
    }

    defaultConfig {
        minSdk = 35
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    buildFeatures {
        compose = true
    }

    // Enables createComposeRule() under Robolectric for this module's own Compose UI tests.
    testOptions {
        unitTests {
            isIncludeAndroidResources = true
        }
    }

    // AGP singleVariant opt-in: declare the single publishable variant + its sources jar.
    // MANDATORY — without this, `from(components["release"])` below fails ("'release' property
    // cannot be found"), the #1 JitPack-Android publish failure.
    publishing {
        singleVariant("release") {
            withSourcesJar()
        }
    }
}

dependencies {
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons.extended)
    implementation(libs.androidx.activity.compose)
    implementation(libs.navigation.compose)
    implementation(libs.coil.compose)

    // LIB-04: `api` (not `implementation`) — EditorItemRow is a public composable with a
    // `ReorderableCollectionItemScope` receiver, so an external consumer that calls it needs the
    // reorderable type on its compile classpath. This is the one confirmed public-signature leak.
    api(libs.reorderable)

    // Hilt — bindings-only capability for @Singleton UndoHistoryStore. No @HiltAndroidApp /
    // @AndroidEntryPoint here; the consuming app aggregates the bindings into SingletonComponent.
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)

    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)

    // Robolectric-backed Compose UI test infra — locks the gesture-composition contract directly
    // in this module.
    testImplementation(libs.robolectric)
    testImplementation(platform(libs.androidx.compose.bom))
    testImplementation(libs.androidx.compose.ui.test.junit4)
    testImplementation(libs.androidx.compose.ui.test.manifest)
}

// JitPack android-example publishing form. `from(components["release"])` MUST be inside
// afterEvaluate — the "release" software component is created by the AGP singleVariant("release")
// opt-in above and does not exist at configuration time. groupId/version are what
// publishToMavenLocal writes; JitPack overrides groupId from the repo owner (com.github.Ygaray)
// and version from the resolved ref.
publishing {
    publications {
        register<MavenPublication>("release") {
            groupId = "com.github.Ygaray"
            artifactId = "yahirandroidtaste"
            version = "1.6.0"
            afterEvaluate {
                from(components["release"])
            }
        }
    }
}
