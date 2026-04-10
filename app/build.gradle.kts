plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.umg.muebleria"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.umg.muebleria"
        minSdk = 26
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        // URL base del backend (VB.NET / API). Emulador: usar 10.0.2.2 en lugar de localhost.
        buildConfigField(
            "String",
            "API_BASE_URL",
            "\"https://TU_SERVIDOR_O_API/api/\""
        )
    }

    buildTypes {
        debug {
            isMinifyEnabled = false
            // Ejemplo desarrollo local (HTTP): descomentar y ajustar puerto cuando exista la API
            // buildConfigField("String", "API_BASE_URL", "\"http://10.0.2.2:PUERTO/api/\"")
        }
        release {
            isMinifyEnabled = true
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
        buildConfig = true
    }
}

dependencies {
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.11.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.7.0")

    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")

    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
}
