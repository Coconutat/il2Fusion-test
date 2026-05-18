plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
}

import java.util.Properties

val il2fusionVersionCode = providers.gradleProperty("IL2FUSION_VERSION_CODE").get().toInt()
val il2fusionVersionName = providers.gradleProperty("IL2FUSION_VERSION_NAME").get()
val signingProperties = Properties().apply {
    file("signing/signing.properties").inputStream().use(::load)
}

android {
    namespace = "com.tools.il2fusion"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.tools.il2fusion"
        minSdk = 31
        targetSdk = 35
        versionCode = il2fusionVersionCode
        versionName = il2fusionVersionName
        
        // 1. 修改这里：让 CMake 编译出对应架构的 .so 动态库
        ndk {
            abiFilters += listOf("arm64-v8a", "x86", "x86_64")
        }

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    // 2. 新增这里：开启 ABI 分包，这样 Gradle 就会为你生成多个独立的 APK 文件
    splits {
        abi {
            isEnable = true
            reset()
            include("arm64-v8a", "x86", "x86_64")
            isUniversalApk = false // 如果你需要额外生成一个包含所有架构的庞大通用APK，可以将此改为 true
        }
    }

    signingConfigs {
        create("projectFixed") {
            storeFile = file(signingProperties.getProperty("storeFile"))
            storePassword = signingProperties.getProperty("storePassword")
            keyAlias = signingProperties.getProperty("keyAlias")
            keyPassword = signingProperties.getProperty("keyPassword")
        }
    }

    buildTypes {
        debug {
            signingConfig = signingConfigs.getByName("projectFixed")
        }
        release {
            signingConfig = signingConfigs.getByName("projectFixed")
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
    // Pack LSPosed descriptors from src/main/assets, and allow optional jniLibs at module root
    sourceSets {
        getByName("main") {
            assets.srcDir("src/main/assets")
            jniLibs.srcDirs("src/main/jniLibs", "${projectDir}/jniLibs")
        }
    }
    externalNativeBuild {
        cmake {
            path = file("src/main/cpp/CMakeLists.txt")
        }
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.datastore.preferences)
    implementation(libs.androidx.navigation.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material.icons.extended)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.okhttp)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
    // hook
    compileOnly(libs.xposed)
}
