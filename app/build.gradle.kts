plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose) // Giữ nguyên theo ý bạn
    id("com.google.gms.google-services")
}

android {
    namespace = "com.dacs3.smartmoney"

    // Sửa lỗi cú pháp: compileSdk chỉ cần số 34 hoặc 35 để ổn định nhất hiện tại
    compileSdk = 36

    defaultConfig {
        applicationId = "com.dacs3.smartmoney"
        minSdk = 24
        targetSdk = 36 // Đưa về 34 để khớp với compileSdk cho ổn định
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
    }
    buildFeatures {
        compose = true
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.navigation.compose)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)

    // Giữ nguyên các thư viện bạn đã thêm
    implementation("androidx.compose.material:material-icons-extended")
    implementation(platform("com.google.firebase:firebase-bom:33.0.0"))
    implementation("com.google.firebase:firebase-auth-ktx")
    implementation("com.google.firebase:firebase-firestore-ktx")

    // BẮT BUỘC: Thêm dòng này để Firebase không làm văng App khi dùng Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-play-services:1.7.3")

    // DataStore for Settings
    implementation(libs.androidx.datastore.preferences)

    // Image loading
    implementation(libs.coil.compose)
}