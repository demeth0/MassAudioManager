plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    
}

android {
    namespace = "com.demeth.massaudioplayer"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.demeth.massaudioplayer"
        minSdk = 30
        targetSdk = 34
        versionCode = 2
        versionName = "5.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles( getDefaultProguardFile("proguard-android-optimize.txt"),
            "proguard-rules.pro")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    buildFeatures {
        viewBinding = true
        dataBinding = true
    }

}

dependencies {
    implementation(libs.bundles.androidx)
    implementation(libs.material)
    implementation(libs.androidx.core.ktx)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}