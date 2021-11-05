plugins {
    id("dev.fritz2.fritz2-gradle") version "0.13"
}

repositories {
    mavenLocal()
    mavenCentral()
}

kotlin {
    jvm()
    js(IR) {
        browser {
            commonWebpackConfig {
                cssSupport.enabled = true
            }
        }
    }.binaries.executable()

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation("dev.fritz2:core:0.13")
                implementation("dev.fritz2:tailwind-core:0.14-SNAPSHOT")
                // see https://components.fritz2.dev/
                // implementation("dev.fritz2:components:0.13")
            }
        }
        val jvmMain by getting {
            dependencies {
            }
        }
        val jsMain by getting {
            dependencies {
                // tailwind
                implementation(npm("postcss", "8.3.5"))
                implementation(npm("postcss-loader", "4.2.0")) // 5.0.0 seems not to work
                implementation(npm("autoprefixer", "10.2.6"))
                implementation(npm("tailwindcss", "2.2.16"))
                implementation(npm("@tailwindcss/forms", "0.3.3"))
            }
        }
    }
}