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
        versionName = "4.0 Test T:V4"

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
    }
}

dependencies {
    implementation("com.google.code.gson:gson:2.11.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.12.0")
    implementation("androidx.compose.material3:material3:1.3.1")
    implementation("com.github.bumptech.glide:glide:4.16.0")
    implementation("com.github.Dimezis:BlurView:version-2.0.5")
    implementation("org.apache.commons:commons-lang3:3.17.0")
    implementation("org.xerial:sqlite-jdbc:3.47.1.0")
    implementation("androidx.work:work-runtime:2.8.1")
    implementation("com.microsoft.clarity:clarity:3.0.1")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    annotationProcessor("com.github.bumptech.glide:compiler:4.16.0")
}