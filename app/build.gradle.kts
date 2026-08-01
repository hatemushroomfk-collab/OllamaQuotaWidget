plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.example.ollamaquotawidget"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.ollamaquotawidget"
        minSdk = 26
        targetSdk = 34
        versionCode = 6
        versionName = "1.6"
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
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
    buildFeatures {
        viewBinding = true
    }
}

dependencies {
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.11.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")

    // Jsoup for HTML parsing
    implementation("org.jsoup:jsoup:1.17.2")
    
    // Preferences DataStore / SharedPrefs
    implementation("androidx.preference:preference-ktx:1.2.1")

    // EncryptedSharedPreferences (쿠키 암호화 저장)
    implementation("androidx.security:security-crypto:1.1.0-alpha06")
}
