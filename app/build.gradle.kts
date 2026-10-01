plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt)
    alias(libs.plugins.detekt)
    alias(libs.plugins.kover)
}

android {
    namespace = "com.caderninho.app"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.caderninho.app"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
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

    implementation(libs.androidx.datastore.preferences)
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
