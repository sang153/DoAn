plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.google.gms.google.services)
}

android {
    namespace = "com.example.truongtansang_dh52111660"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.truongtansang_dh52111660"
        minSdk = 29
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
}

dependencies {
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("com.google.android.material:material:1.11.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    
    // Firebase
    implementation("com.google.firebase:firebase-auth:22.3.0")
    implementation("com.google.firebase:firebase-analytics:21.5.0")
    
    // reCAPTCHA
    implementation("com.google.android.gms:play-services-safetynet:18.0.1")
    implementation("com.google.android.gms:play-services-auth:20.7.0")

    // Pie chart
    implementation("com.github.PhilJay:MPAndroidChart:v3.1.0")
}
