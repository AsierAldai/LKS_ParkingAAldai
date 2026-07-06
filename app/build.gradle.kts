plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.ksp)
    alias(libs.plugins.google.services)
    alias(libs.plugins.firebase.crashlytics)
    alias(libs.plugins.sonar)
    alias(libs.plugins.kover)
}

sonar {
    properties {
        property("sonar.projectKey", "AsierAldai_LKS_ParkingAAldai")
        property("sonar.projectName", "LKS_ParkingAAldai")
        property("sonar.organization", "asieraldai")
        property(
            "sonar.coverage.jacoco.xmlReportPaths",
            layout.buildDirectory.file("reports/kover/report.xml").get().asFile.path
        )
        property(
            "sonar.coverage.exclusions",
            listOf(
                "**/MainActivity*",
                "**/Navigation.kt",
                "**/ui/screens/**",
                "**/ui/components/**",
                "**/ui/theme/**",
                "**/notifications/NotificationHelper.kt",
                "**/workers/NotificationWorker.kt",
                "**/data/repository/FirebaseRepository.kt",
                "**/auth/AuthManager.kt",
                "**/auth/AuthDataSource.kt"
            ).joinToString(",")
        )
    }
}

android {
    namespace = "com.lksnext.ParkingAAldai"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.lksnext.ParkingAAldai"
        minSdk = 24
        targetSdk = 35
        versionCode = 3
        versionName = "1.3"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        compose = true
    }
}

kover {
    reports {
        filters {
            excludes {
                classes(
                    "com.lksnext.ParkingAAldai.MainActivity*",
                    "com.lksnext.ParkingAAldai.ComposableSingletons*",
                    "com.lksnext.ParkingAAldai.NavigationKt*",
                    "com.lksnext.ParkingAAldai.ui.screens.*",
                    "com.lksnext.ParkingAAldai.ui.components.*",
                    "com.lksnext.ParkingAAldai.ui.theme.*",
                    "com.lksnext.ParkingAAldai.notifications.NotificationHelper",
                    "com.lksnext.ParkingAAldai.workers.NotificationWorker",
                    "com.lksnext.ParkingAAldai.data.repository.FirebaseRepository*",
                    "com.lksnext.ParkingAAldai.auth.*"
                )
            }
        }
    }
}

dependencies {
    // Apply the Compose BOM to all configurations
    val composeBom = platform(libs.androidx.compose.bom)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.activity.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.ui)
    implementation(libs.androidx.compose.foundation)
    implementation(composeBom)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons.extended)
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    implementation(libs.androidx.work.runtime.ktx)
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.analytics)
    implementation(libs.firebase.crashlytics)
    implementation(libs.firebase.auth)
    implementation("com.google.firebase:firebase-perf")
    implementation(libs.firebase.firestore)

    debugImplementation(composeBom)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)

    testImplementation(libs.junit)
    testImplementation(libs.mockk)
    testImplementation(libs.kotlinx.coroutines.test)


    androidTestImplementation(composeBom)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)

    ksp(libs.androidx.room.compiler)
}
