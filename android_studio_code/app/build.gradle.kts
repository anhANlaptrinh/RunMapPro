plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.example.runmapproapp"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.runmapproapp"
        minSdk = 26
        targetSdk = 33  // Giảm xuống 33 để tương thích với Mapbox Navigation 2.15.2
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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
}

dependencies {
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.swiperefreshlayout)
    implementation(libs.retrofit)
    implementation(libs.converter.gson)
    implementation(libs.okhttp)
    implementation(libs.okhttp.logging)
    implementation(libs.gson)
    implementation(libs.glide)
    implementation(libs.glide.okhttp3)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    annotationProcessor(libs.glide.compiler)
    
    // Mapbox dependencies
    implementation(libs.mapbox.navigation)
    implementation(libs.mapbox.search.ui)
    // Room Database
    implementation(libs.room.runtime)
    annotationProcessor(libs.room.compiler)
    
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}