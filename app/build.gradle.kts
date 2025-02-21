plugins {
    id("com.android.application")
}

android {
    namespace = "top.srintelligence.wallpaper_generator"
    compileSdk = 35
    defaultConfig {
        applicationId = "top.srintelligence.wallpaper_generator"
        minSdk = 23
        targetSdk = 35
        versionCode = 4
        versionName = "4.0 Test 2025-1-7 T:V4"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        renderscriptTargetApi = 19
        renderscriptSupportModeEnabled = true
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
        isCoreLibraryDesugaringEnabled = true
    }
}

dependencies {
    implementation("com.google.code.gson:gson:2.12.1")
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("com.google.android.material:material:1.12.0")
    implementation("androidx.compose.material3:material3:1.3.1")
    implementation("com.github.bumptech.glide:glide:4.16.0")
    implementation("com.github.Dimezis:BlurView:version-2.0.5")
    implementation("org.apache.commons:commons-lang3:3.17.0")
    implementation("org.xerial:sqlite-jdbc:3.47.1.0")
    implementation("androidx.work:work-runtime:2.10.0")
    implementation("com.microsoft.clarity:clarity:3.1.3")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.2.1")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.6.1")
    annotationProcessor("com.github.bumptech.glide:compiler:4.16.0")
    coreLibraryDesugaring("com.android.tools:desugar_jdk_libs:2.1.4")
}