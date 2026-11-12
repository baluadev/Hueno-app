plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
}

// Ad and product identifiers. Without overrides these are Google's public test ids, which are
// safe to click but earn nothing. Set the real ones in ~/.gradle/gradle.properties or with -P.
fun setting(name: String, fallback: String): String = (project.findProperty(name) as String?) ?: fallback

val adMobAppId = setting("HUENO_ADMOB_APP_ID", "ca-app-pub-3940256099942544~3347511713")
val adBannerUnitId = setting("HUENO_AD_BANNER_UNIT_ID", "ca-app-pub-3940256099942544/9214589741")
val adRewardedUnitId = setting("HUENO_AD_REWARDED_UNIT_ID", "ca-app-pub-3940256099942544/5224354917")
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
        versionCode = 1
        versionName = "0.1.0"

        manifestPlaceholders["adMobAppId"] = adMobAppId
        buildConfigField("String", "AD_BANNER_UNIT_ID", "\"$adBannerUnitId\"")
        buildConfigField("String", "AD_REWARDED_UNIT_ID", "\"$adRewardedUnitId\"")
        buildConfigField("String", "PRO_PRODUCT_ID", "\"$proProductId\"")
        buildConfigField("String", "FEEDBACK_EMAIL", "\"$feedbackEmail\"")
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
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
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    debugImplementation(libs.androidx.compose.ui.tooling)

    testImplementation(libs.junit)
}
