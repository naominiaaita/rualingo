plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.app.rualingoapplication"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.app.rualingoapplication"
        minSdk = 27
        targetSdk = 36
        versionCode = 2
        versionName = "1.1"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildFeatures {
        buildConfig = true
        resValues = true
    }

    flavorDimensions += "userType"
    productFlavors {
        create("student") {
            dimension = "userType"
            applicationId = "com.rualingo.student"
            versionNameSuffix = "-student"
            resValue("string", "app_name", "Rualingo")
            // Backend role name for the learner app.
            buildConfigField("String", "FLAVOR_TYPE", "\"USER\"")
        }
        create("admin") {
            dimension = "userType"
            applicationId = "com.rualingo.admin"
            versionNameSuffix = "-admin"
            resValue("string", "app_name", "Rualingo Admin")
            buildConfigField("String", "FLAVOR_TYPE", "\"ADMIN\"")
        }
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
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }
}

dependencies {
    // Retrofit & GSON
    implementation(libs.retrofit)
    implementation(libs.retrofit2.converter.gson)

    // Room
    implementation(libs.room.runtime)
    annotationProcessor(libs.room.compiler)

    // Standard Libraries
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation(libs.gridlayout)

    // Glide for image loading
    implementation("com.github.bumptech.glide:glide:4.16.0")
    annotationProcessor("com.github.bumptech.glide:compiler:4.16.0")

    // Testing
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}
