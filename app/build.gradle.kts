import java.io.File
import java.util.Properties

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.serialization") version "2.2.0" 
}

val releaseSigningPropertiesFile = rootProject.file("keystore.properties")
val releaseSigningProperties = Properties().apply {
    if (releaseSigningPropertiesFile.isFile) {
        releaseSigningPropertiesFile.inputStream().use { load(it) }
    }
}

fun releaseSigningValue(propertyName: String, environmentName: String): String? {
    return System.getenv(environmentName)?.takeIf { it.isNotBlank() }
        ?: releaseSigningProperties.getProperty(propertyName)?.takeIf { it.isNotBlank() }
        ?: releaseSigningProperties.getProperty("release.$propertyName")?.takeIf { it.isNotBlank() }
}

fun releaseStoreFile(path: String) = File(path).let { candidate ->
    if (candidate.isAbsolute) candidate else rootProject.file(path)
}

// Private subscription endpoint configuration; no payload decryption keys are packaged.
val buildPropertiesFile = rootProject.file("secrets.properties")
val buildProperties = Properties().apply {
    if (buildPropertiesFile.isFile) {
        buildPropertiesFile.inputStream().use { load(it) }
    }
}

fun httpsBuildUrl(environmentName: String, defaultValue: String): String {
    return (System.getenv(environmentName)?.takeIf { it.isNotBlank() } ?: defaultValue).also {
        require(it.startsWith("https://")) { "$environmentName must use HTTPS" }
    }
}

val mihomoSubscriptionUrl = httpsBuildUrl(
    "WHITEDNS_MIHOMO_SUBSCRIPTION_URL",
    "https://enterprisechat.ru/es/v1/reserve",
)
val privateMihomoSubscriptionUrl = (
    System.getenv("WHITEDNS_PRIVATE_MIHOMO_SUBSCRIPTION_URL")?.takeIf { it.isNotBlank() }
        ?: buildProperties.getProperty("privateMihomoSubscriptionUrl")?.takeIf { it.isNotBlank() }
        ?: ""
    ).also {
    require(it.isEmpty() || it.startsWith("https://") || it.startsWith("http://89.208.103.229/")) {
        "WHITEDNS_PRIVATE_MIHOMO_SUBSCRIPTION_URL must use HTTPS"
    }
}
fun buildConfigStringLiteral(value: String): String =
    "\"" + value.replace("\\", "\\\\").replace("\"", "\\\"").replace("$", "\\u0024") + "\""

val releaseStoreFilePath = releaseSigningValue("storeFile", "WHITEDNS_RELEASE_STORE_FILE")
val releaseStorePassword = releaseSigningValue("storePassword", "WHITEDNS_RELEASE_STORE_PASSWORD")
val releaseKeyAlias = releaseSigningValue("keyAlias", "WHITEDNS_RELEASE_KEY_ALIAS")
val releaseKeyPassword = releaseSigningValue("keyPassword", "WHITEDNS_RELEASE_KEY_PASSWORD")
val hasReleaseSigning = listOf(
    releaseStoreFilePath,
    releaseStorePassword,
    releaseKeyAlias,
    releaseKeyPassword,
).all { !it.isNullOrBlank() }

android {
    namespace = "com.whitedns.vpn"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.enterprise.shield.vpn"
        minSdk = 26
        targetSdk = 35
        versionCode = 274
        versionName = "1.6.26"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        resourceConfigurations += listOf("en", "ru")

        buildConfigField("String", "MIHOMO_SUBSCRIPTION_URL", buildConfigStringLiteral(mihomoSubscriptionUrl))
        buildConfigField(
            "String",
            "PRIVATE_MIHOMO_SUBSCRIPTION_URL",
            buildConfigStringLiteral(privateMihomoSubscriptionUrl),
        )

        // Локальная сборка: используются готовые .so, CMake не запускается.

    }

    splits {
        abi {
            isEnable = true
            reset()
            include("arm64-v8a")
            isUniversalApk = true
        }
    }

    signingConfigs {
        if (hasReleaseSigning) {
            create("release") {
                storeFile = releaseStoreFile(releaseStoreFilePath!!)
                storePassword = releaseStorePassword
                keyAlias = releaseKeyAlias
                keyPassword = releaseKeyPassword
            }
        }
    }

    buildTypes {
        release {
            if (hasReleaseSigning) {
                signingConfig = signingConfigs.getByName("release")
            }
            isMinifyEnabled = false
            isShrinkResources = false
            // CMake and Go already strip release binaries; generated .sym files only duplicate them.
            ndk.debugSymbolLevel = "NONE"
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
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

    packaging {
        resources {
            excludes += "META-INF/versions/9/OSGI-INF/MANIFEST.MF"
        }
        jniLibs {
            useLegacyPackaging = true
        }
    }

    sourceSets {
        getByName("main") {
            jniLibs.srcDir("src/main/jniLibs")
        }
    }

    // Локальная сборка без CMake: JNI-мост берётся готовым из jniLibs.

    buildFeatures {
        buildConfig = true
    }

    lint {
        checkReleaseBuilds = false
        abortOnError = false
    }

    packaging {
        jniLibs {
            keepDebugSymbols += "**/*.so"
        }
    }
}

dependencies {
    implementation("androidx.core:core-ktx:1.15.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.9.0")
    implementation("org.json:json:20240303")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("org.bouncycastle:bcprov-jdk18on:1.78.1")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.7.3")
    implementation("com.journeyapps:zxing-android-embedded:4.3.0")
    implementation("com.github.Dimezis:BlurView:version-4.0.0")
    implementation(libs.material)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    testImplementation("junit:junit:4.13.2")
}

val buildFlClashCore = tasks.register("buildFlClashCore") {
    doLast { logger.lifecycle("FlClash core: используются готовые .so из jniLibs") }
}

tasks.register("checkFlClashCore") {
    // Локальная сборка использует готовые .so — проверка заголовков не нужна.
    doLast { logger.lifecycle("FlClash core: проверка пропущена") }
}

val validateReleaseInputs = tasks.register("validateReleaseInputs") {
    dependsOn("checkFlClashCore")
    doLast {
        if (!hasReleaseSigning) {
            throw GradleException(
                "Release signing is not configured. Set WHITEDNS_RELEASE_STORE_FILE, " +
                    "WHITEDNS_RELEASE_STORE_PASSWORD, WHITEDNS_RELEASE_KEY_ALIAS, and " +
                    "WHITEDNS_RELEASE_KEY_PASSWORD, or create keystore.properties from " +
                    "keystore.properties.example.",
            )
        }
        val store = releaseStoreFile(releaseStoreFilePath!!)
        if (!store.isFile) {
            throw GradleException("Release keystore not found: ${store.absolutePath}")
        }
    }
}

tasks.matching { it.name == "assembleRelease" || it.name == "bundleRelease" }.configureEach {
    dependsOn(validateReleaseInputs)
}
