plugins {
    // Плагіни через version catalogs
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.kotlin.android)
    alias(libs.plugins.google.gms.google.services)

    // Якщо працюємо з Kotlin + Glide (круто мати kapt)
    id("kotlin-kapt")
}

android {
    namespace = "com.example.gitschool"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.gitschool"
        minSdk = 24
        targetSdk = 34
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
    kotlinOptions {
        jvmTarget = "1.8"
    }
}

dependencies {

    // Jsoup (залиште, якщо потрібен, тільки один раз)
    implementation("org.jsoup:jsoup:1.14.3") // Або видаліть, якщо не використовуєте

    // Retrofit + Gson (залиште, якщо потрібен для мережевих запитів)
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")

    // Glide (тільки необхідні рядки)
    implementation("com.github.bumptech.glide:glide:4.15.1")
    implementation(libs.google.firebase.firestore.ktx)
    implementation(libs.androidx.foundation.android)
    kapt("com.github.bumptech.glide:compiler:4.15.1") // Використовуємо kapt замість annotationProcessor
    implementation("com.google.android.flexbox:flexbox:3.0.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.6.4")

    // Firebase (один BOM та тільки необхідні сервіси)
    implementation(platform("com.google.firebase:firebase-bom:33.7.0")) // Тільки один BOM, новіший
    implementation("com.google.firebase:firebase-auth-ktx")           // Для автентифікації
    implementation("com.google.firebase:firebase-database-ktx")       // Для Realtime Database (замість Firestore/Storage)
    implementation("com.google.android.gms:play-services-auth:21.3.0") // Для Google Sign-In

    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.0") // Перевірте останні стабільні версії, якщо бажаєте
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.0")

    implementation("com.pierfrancescosoffritti.androidyoutubeplayer:core:12.1.0")


    implementation("com.google.mlkit:translate:17.0.1")

    // Material, AppCompat, тощо (з version catalogs - це добре)
    implementation(libs.material)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.activity)

    // Testing
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}
