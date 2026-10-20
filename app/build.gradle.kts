plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.mm.mmaps"
    compileSdk {
        version = release(37)
    }

    defaultConfig {
        applicationId = "com.mm.mmaps"
        minSdk = 34
        targetSdk = 37
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            optimization {
                enable = false
            }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
    implementation(libs.activity.ktx)
    implementation(libs.appcompat)
    implementation(libs.constraintlayout)
    implementation(libs.material)
    testImplementation(libs.junit)
    androidTestImplementation(libs.espresso.core)
    androidTestImplementation(libs.ext.junit)

    // Google Play Services Maps
    implementation("com.google.android.gms:play-services-maps:18.2.0")
    // Location services
    implementation("com.google.android.gms:play-services-location:21.2.0")
}