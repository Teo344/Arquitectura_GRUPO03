plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
}

android {
    // Mismo paquete base que los clientes de consola y escritorio
    namespace = "ec.edu.monster"
    compileSdk {
        version = release(37)
    }

    defaultConfig {
        applicationId = "ec.edu.monster.cliente_movil"
        minSdk = 24
        targetSdk = 37
        versionCode = 1
        versionName = "1.0"

        // URL del servidor SOAP: se cambia en gradle.properties (servidor.url)
        val urlServidor = providers.gradleProperty("servidor.url")
            .getOrElse("http://10.0.2.2:8080/WS_CON_UNI_JAVA_SOAP")
        buildConfigField("String", "URL_SERVIDOR", "\"$urlServidor\"")
    }

    buildTypes {
        release {
            optimization {
                enable = true
                packageScope = setOf("androidx.**", "kotlin.**", "kotlinx.**")
            }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
}

dependencies {
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.core.ktx)
    debugImplementation(libs.androidx.compose.ui.tooling)
}
