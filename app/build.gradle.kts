plugins {
  id("com.android.application")
  id("org.jetbrains.kotlin.android")
  id("org.jetbrains.kotlin.kapt")
  id("org.jetbrains.kotlin.plugin.compose")
  id("com.google.dagger.hilt.android")
}

android {
  namespace = "com.streamlite"
  compileSdk = 36

  defaultConfig {
    applicationId = "com.streamlite"
    minSdk = 31
    targetSdk = 36
    versionCode = 1
    versionName = "1.0.0"
  }

  buildFeatures {
    compose = true
    buildConfig = true
  }
  compileOptions {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
  }
  packaging {
    resources.excludes += "/META-INF/{AL2.0,LGPL2.1}"
  }
}

kotlin {
  jvmToolchain(17)
}

dependencies {
  implementation("androidx.core:core-ktx:1.15.0")
  implementation("androidx.activity:activity-compose:1.10.1")
  implementation(platform("androidx.compose:compose-bom:2024.04.01"))
  implementation("androidx.compose.ui:ui")
  implementation("androidx.compose.ui:ui-tooling-preview")
  implementation("androidx.compose.material3:material3")
  implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.7")
  implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.7")
  implementation("androidx.hilt:hilt-navigation-compose:1.2.0")
  implementation("androidx.datastore:datastore-preferences:1.1.2")
  implementation("androidx.core:core-splashscreen:1.0.1")
  implementation("androidx.lifecycle:lifecycle-service:2.8.7")
  implementation("com.google.android.material:material:1.12.0")

  implementation("com.google.dagger:hilt-android:2.57.2")
  kapt("com.google.dagger:hilt-compiler:2.57.2")

  implementation("com.github.pedroSG94.RootEncoder:library:2.7.0")
  implementation("androidx.lifecycle:lifecycle-runtime-compose:2.8.7")

  debugImplementation("androidx.compose.ui:ui-tooling")
}

kapt {
  correctErrorTypes = true
}
