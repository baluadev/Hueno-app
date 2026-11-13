import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.google.services)
}

// Upload key for release builds. Kept outside the repository; never commit it. The default path
// can be overridden with the Gradle property HUENO_KEYSTORE_PROPERTIES. Without the file, release
// builds are produced unsigned. Google Play holds the app signing key (Play App Signing), so a lost
// upload key can be reset through Play Console support.
val keystoreProperties = Properties().apply {
    val path = (project.findProperty("HUENO_KEYSTORE_PROPERTIES") as String?)
        ?: "${System.getProperty("user.home")}/Documents/keystore/hueno-signing/keystore.properties"
    val file = File(path)
    if (file.isFile) file.inputStream().use { load(it) }
}

// Ad and product identifiers. A Gradle property (~/.gradle/gradle.properties or -P) overrides any
// of them.
fun setting(name: String, fallback: String): String = (project.findProperty(name) as String?) ?: fallback

// Release builds use Hueno's AdMob ids. Debug builds keep Google's public test ids: real ads seen
// or tapped on the team's own phones count as invalid traffic and can get the AdMob account
// limited. Ad ids are not secret; they ship inside every APK.
val releaseAdMobAppId = setting("HUENO_ADMOB_APP_ID", "ca-app-pub-8681872943360035~2789069929")
val releaseAdBannerUnitId = setting("HUENO_AD_BANNER_UNIT_ID", "ca-app-pub-8681872943360035/6803356613")
val releaseAdRewardedUnitId = setting("HUENO_AD_REWARDED_UNIT_ID", "ca-app-pub-8681872943360035/6420213231")
val testAdMobAppId = setting("HUENO_DEBUG_ADMOB_APP_ID", "ca-app-pub-3940256099942544~3347511713")
val testAdBannerUnitId = setting("HUENO_DEBUG_AD_BANNER_UNIT_ID", "ca-app-pub-3940256099942544/9214589741")
val testAdRewardedUnitId = setting("HUENO_DEBUG_AD_REWARDED_UNIT_ID", "ca-app-pub-3940256099942544/5224354917")
val proProductId = setting("HUENO_PRO_PRODUCT_ID", "pro_lifetime")

// Address for the "Send feedback" row in Settings. Empty hides the row.
val feedbackEmail = setting("HUENO_FEEDBACK_EMAIL", "")

android {
    namespace = "com.hueno.coloridentifier"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.hueno.coloridentifier"
        minSdk = 26
        targetSdk = 36
        versionCode = 3
        versionName = "0.1.0"

        buildConfigField("String", "PRO_PRODUCT_ID", "\"$proProductId\"")
        buildConfigField("String", "FEEDBACK_EMAIL", "\"$feedbackEmail\"")
    }

    signingConfigs {
        if (keystoreProperties.containsKey("storeFile")) {
            create("upload") {
                storeFile = File(keystoreProperties.getProperty("storeFile"))
                storePassword = keystoreProperties.getProperty("storePassword")
                keyAlias = keystoreProperties.getProperty("keyAlias")
                keyPassword = keystoreProperties.getProperty("keyPassword")
            }
        }
    }

    buildTypes {
        debug {
            manifestPlaceholders["adMobAppId"] = testAdMobAppId
            buildConfigField("String", "AD_BANNER_UNIT_ID", "\"$testAdBannerUnitId\"")
            buildConfigField("String", "AD_REWARDED_UNIT_ID", "\"$testAdRewardedUnitId\"")
        }
        release {
            manifestPlaceholders["adMobAppId"] = releaseAdMobAppId
            buildConfigField("String", "AD_BANNER_UNIT_ID", "\"$releaseAdBannerUnitId\"")
            buildConfigField("String", "AD_REWARDED_UNIT_ID", "\"$releaseAdRewardedUnitId\"")
            signingConfig = signingConfigs.findByName("upload")
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    // Lists the languages in res/ for Android 13's per-app language setting. The default (values/)
    // is English, as declared in res/resources.properties.
    androidResources {
        generateLocaleConfig = true
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }
}

kotlin {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
    }
}

dependencies {
    implementation(project(":core-color"))
    implementation(project(":camera"))
    implementation(project(":data"))
    implementation(project(":monetization"))
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.analytics)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.core.splashscreen)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    debugImplementation(libs.androidx.compose.ui.tooling)

    testImplementation(libs.junit)
}
