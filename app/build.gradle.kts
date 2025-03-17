import java.util.*

plugins {
    id("com.android.application")
}


tasks.register<Task>("generateRandomDictionary") {
    doLast {
        val outputFile = File(projectDir, "random.txt")
        val random = Random()

        val firstCharPool = ('a'..'z').toList() + ('A'..'Z').toList() + listOf('_', '$')

        val safeConfusingChars = listOf(
            'а', 'е', 'о', 'р', 'с', 'х', 'А', 'В', 'Е', 'К', 'М', 'Н', 'О', 'Р', 'С', 'Т', 'У', 'Х',

            'α', 'β', 'γ', 'δ', 'ε', 'η', 'θ', 'ι', 'κ', 'λ', 'μ', 'ν', 'ο', 'π', 'ρ', 'σ', 'τ', 'φ', 'χ', 'ψ', 'ω',
            'Α', 'Β', 'Γ', 'Δ', 'Ε', 'Ζ', 'Η', 'Θ', 'Ι', 'Κ', 'Λ', 'Μ', 'Ν', 'Ξ', 'Ο', 'Π', 'Ρ', 'Σ', 'Τ', 'Υ', 'Φ', 'Χ', 'Ψ', 'Ω',

            'ℂ', 'ℕ', 'ℙ', 'ℚ', 'ℝ', 'ℤ',

            'ｘ', 'ｙ', 'ｚ', 'ａ', 'ｂ', 'ｃ', 'ｄ', 'ｅ', 'ｆ', 'ｇ', 'ｈ', 'ｉ', 'ｊ', 'ｋ', 'ｌ', 'ｍ', 'ｎ', 'ｏ', 'ｐ', 'ｑ', 'ｒ', 'ｓ', 'ｔ', 'ｕ', 'ｖ', 'ｗ',
            'Ａ', 'Ｂ', 'Ｃ', 'Ｄ', 'Ｅ', 'Ｆ', 'Ｇ', 'Ｈ', 'Ｉ', 'Ｊ', 'Ｋ', 'Ｌ', 'Ｍ', 'Ｎ', 'Ｏ', 'Ｐ', 'Ｑ', 'Ｒ', 'Ｓ', 'Ｔ', 'Ｕ', 'Ｖ', 'Ｗ', 'Ｘ', 'Ｙ', 'Ｚ',

            'ā', 'ă', 'ą', 'ć', 'ĉ', 'ċ', 'č', 'ď', 'đ', 'ē', 'ĕ', 'ė', 'ę', 'ě', 'ĝ', 'ğ', 'ġ', 'ģ', 'ĥ', 'ħ', 'ĩ', 'ī', 'ĭ', 'į',
            'Ā', 'Ă', 'Ą', 'Ć', 'Ĉ', 'Ċ', 'Č', 'Ď', 'Đ', 'Ē', 'Ĕ', 'Ė', 'Ę', 'Ě', 'Ĝ', 'Ğ', 'Ġ', 'Ģ', 'Ĥ', 'Ħ', 'Ĩ', 'Ī', 'Ĭ', 'Į'
        )

        val restCharsPool = firstCharPool + ('0'..'9').toList() + safeConfusingChars

        outputFile.writer(Charsets.UTF_8).use { writer ->
            repeat(2000) {
                val firstChar = firstCharPool[random.nextInt(firstCharPool.size)]

                val length = random.nextInt(8) + 3

                val restChars = mutableListOf<Char>()
                var hasSpecialChar = false

                for (i in 1 until length) {
                    if (!hasSpecialChar && i > length / 2 && random.nextBoolean()) {
                        restChars.add(safeConfusingChars[random.nextInt(safeConfusingChars.size)])
                        hasSpecialChar = true
                    } else {
                        restChars.add(restCharsPool[random.nextInt(restCharsPool.size)])
                    }
                }

                if (!hasSpecialChar && length > 1) {
                    val pos = random.nextInt(restChars.size)
                    restChars[pos] = safeConfusingChars[random.nextInt(safeConfusingChars.size)]
                }

                writer.write("$firstChar${restChars.joinToString("")}\n")
            }
        }

        println("Created random dictionary at ${outputFile.absolutePath}")
    }
}

tasks.named("preBuild") {
    dependsOn("generateRandomDictionary")
}

android {
    namespace = "top.srintelligence.wallpaper_generator"
    compileSdk = 35
    defaultConfig {
        applicationId = "top.srintelligence.wallpaper_generator"
        minSdk = 23
        targetSdk = 35
        versionCode = 4
        versionName = "Release 2025.3.18"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        renderscriptTargetApi = 19
        renderscriptSupportModeEnabled = true
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }

        debug {
            isMinifyEnabled = false
            isShrinkResources = false
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