
buildscript {
    dependencies {
        // AGP is added via plugins in app module
    }
}
plugins {
    id("com.android.application") version "8.5.2" apply false
    kotlin("android") version "1.9.24" apply false
    id("org.jlleitschuh.gradle.ktlint") version "12.1.1" apply false
}
