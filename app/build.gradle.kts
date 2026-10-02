plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt)
    alias(libs.plugins.detekt)
    alias(libs.plugins.kover)
}

val appVersionName = rootProject.file("VERSION").readText().trim()
val appVersionCode = rootProject.file("VERSION_CODE").readText().trim().toInt()
val uploadStoreFile = providers.gradleProperty("CADERNINHO_UPLOAD_STORE_FILE")
    .orElse(providers.environmentVariable("CADERNINHO_UPLOAD_STORE_FILE"))
val uploadStorePassword = providers.gradleProperty("CADERNINHO_UPLOAD_STORE_PASSWORD")
    .orElse(providers.environmentVariable("CADERNINHO_UPLOAD_STORE_PASSWORD"))
val uploadKeyAlias = providers.gradleProperty("CADERNINHO_UPLOAD_KEY_ALIAS")
    .orElse(providers.environmentVariable("CADERNINHO_UPLOAD_KEY_ALIAS"))
val uploadKeyPassword = providers.gradleProperty("CADERNINHO_UPLOAD_KEY_PASSWORD")
    .orElse(providers.environmentVariable("CADERNINHO_UPLOAD_KEY_PASSWORD"))
val releaseSigningValues = listOf(
    uploadStoreFile,
    uploadStorePassword,
    uploadKeyAlias,
    uploadKeyPassword
)
val hasReleaseSigning = releaseSigningValues.all { it.isPresent }
val signedReleaseTasks = setOf("bundleRelease", "assembleRelease")
val releaseBuildRequested = gradle.startParameter.taskNames.any {
    it.substringAfterLast(':') in signedReleaseTasks
}

check(!releaseBuildRequested || hasReleaseSigning) {
    "Release signing is required. Configure the CADERNINHO_UPLOAD_* Gradle properties " +
        "or environment variables documented in docs/release/play-store-checklist.md."
}

android {
    namespace = "com.caderninho.app"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.caderninho.app"
        minSdk = 24
        targetSdk = 36
        versionCode = appVersionCode
        versionName = appVersionName

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    signingConfigs {
        if (hasReleaseSigning) {
            create("release") {
                storeFile = file(uploadStoreFile.get())
                storePassword = uploadStorePassword.get()
                keyAlias = uploadKeyAlias.get()
                keyPassword = uploadKeyPassword.get()
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            signingConfig = signingConfigs.findByName("release")
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
        isCoreLibraryDesugaringEnabled = true
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
    }
    ksp {
        arg("room.schemaLocation", "$projectDir/schemas")
    }
    lint {
        warningsAsErrors = true
        disable += setOf(
            "AndroidGradlePluginVersion",
            "GradleDependency",
            "NewerVersionAvailable",
            "OldTargetApi"
        )
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.material.icons.extended)
    implementation(libs.androidx.navigation.compose)

    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)

    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    implementation(libs.androidx.hilt.navigation.compose)

    implementation(libs.androidx.work.runtime.ktx)
    implementation(libs.androidx.hilt.work)
    ksp(libs.androidx.hilt.compiler)

    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.turbine)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.room.testing)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)

    coreLibraryDesugaring(libs.desugar.jdk.libs)
}

detekt {
    config.setFrom(files("$rootDir/config/detekt/detekt.yml"))
    buildUponDefaultConfig = true
    parallel = true
    ignoreFailures = false
}

tasks.withType<io.gitlab.arturbosch.detekt.Detekt>().configureEach {
    jvmTarget = "11"
    reports {
        xml.required.set(true)
        html.required.set(true)
        sarif.required.set(true)
        txt.required.set(false)
    }
}

kover {
    reports {
        filters {
            excludes {
                classes(
                    "*.R",
                    "*.R$*",
                    "*.BuildConfig",
                    "*.Manifest*",
                    "*.*ScreenKt",
                    "*.*ScreenKt$*",
                    "*.*DialogKt",
                    "*.*DialogKt$*",
                    "*.ui.screens.sale.SaleCardKt",
                    "*.ui.screens.sale.SaleCardKt$*",
                    "*.ui.screens.sale.SaleMenuActions",
                    "*.LedgerApplication",
                    "*.MainActivity",
                    "*.MainActivity$*",
                    "*.ui.theme.*",
                    "*.ui.navigation.*",
                    "*.di.*",
                    "*.data.local.dao.*",
                    "*.data.local.LedgerDatabase",
                    "*.data.local.LedgerDatabase$*",
                    "*.data.local.LedgerDatabase_Impl",
                    "*.data.local.LedgerDatabase_Impl$*",
                    "*.data.local.Converters",
                    "*.util.WhatsAppLauncher",
                    "*.notification.ChargeNotification",
                    "*.notification.WorkManagerChargeReminderScheduler",
                    "*.notification.WorkManagerChargeReminderScheduler$*",
                    "*.notification.ChargeReminderWorker",
                    "*.notification.ChargeReminderWorker$*",
                    "*.ui.components.*",
                    "*.ui.screens.sale.SaleFormEditor",
                    "*.ui.screens.sale.SaleFormState",
                    "*.ui.screens.sale.SaleFormEvent*",
                    "*.ui.screens.sale.SaleItemUi",
                    "*.ui.screens.sale.ClientDetailActions",
                    "*ComposableSingletons*",
                    "*ComposableSingletons*$*",
                    "*_Factory",
                    "*_Factory$*",
                    "*_MembersInjector",
                    "*_MembersInjector$*",
                    "*_HiltModules",
                    "*_HiltModules$*",
                    "*_HiltModules_*",
                    "*_HiltComponents*",
                    "Hilt_*",
                    "Hilt_*$*",
                    "*.Hilt_*",
                    "*.Hilt_*$*",
                )
                packages(
                    "com.caderninho.app.ui.theme",
                    "com.caderninho.app.ui.navigation",
                    "com.caderninho.app.ui.components",
                    "com.caderninho.app.di",
                    "hilt_aggregated_deps",
                    "dagger.hilt.internal.aggregatedroot.codegen",
                )
            }
        }
        verify {
            rule("MVVM domain and data line coverage") {
                minBound(80)
            }
        }
    }
}
